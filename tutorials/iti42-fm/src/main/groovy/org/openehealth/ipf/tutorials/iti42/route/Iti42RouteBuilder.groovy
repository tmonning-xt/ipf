/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openehealth.ipf.tutorials.iti42.route

import org.apache.camel.Exchange
import org.apache.camel.Message
import org.apache.camel.spring.SpringRouteBuilder
import org.openehealth.ipf.commons.ihe.xds.core.responses.ErrorCode
import org.openehealth.ipf.commons.ihe.xds.core.responses.ErrorInfo
import org.openehealth.ipf.commons.ihe.xds.core.responses.Response
import org.openehealth.ipf.commons.ihe.xds.core.responses.Severity
import org.openehealth.ipf.tutorials.iti42.dataformat.XdsDataFormat

import static org.openehealth.ipf.commons.ihe.xds.core.responses.Status.SUCCESS
import static org.openehealth.ipf.commons.ihe.xds.core.responses.Status.FAILURE
import static org.openehealth.ipf.platform.camel.ihe.xds.XdsCamelValidators.iti42RequestValidator
import static org.openehealth.ipf.platform.camel.ihe.xds.XdsCamelValidators.iti42ResponseValidator
import static org.openehealth.ipf.platform.camel.core.util.Exchanges.resultMessage
import static org.openehealth.ipf.platform.camel.core.util.Exchanges.extractException

public class Iti42RouteBuilder extends SpringRouteBuilder {

    XdsDataFormat dataFormat = new XdsDataFormat()

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
            .process { createErrorResult(it) }
            .nakFlow()

        from('xds-iti42:xds-iti42-service')
            .initFlow('xds-iti42').application('iti42-tutorial')
                .inFormat(dataFormat)
                .outFormat(dataFormat)
            .process(iti42RequestValidator())
            //..DO-SOMETHING
            .process { createSuccessResult(it) }
            .process(iti42ResponseValidator())
            .ackFlow()
    }

    void createSuccessResult(exchange) {
        def response = new Response(SUCCESS)
        createResult(exchange, response)
    }

    void createErrorResult(Exchange exchange) {
        def response = new Response(FAILURE)
        response.setErrors(Arrays.asList(
            new ErrorInfo(
                        ErrorCode.REGISTRY_ERROR,
                        extractException(exchange, false).message,
                        Severity.ERROR,
                        null,
                        null)));
        createResult(exchange, response)
    }

    void createResult(Exchange exchange, Response response){
        Message resultMessage = resultMessage(exchange)
        resultMessage.body = response
        resultMessage.headers = exchange.in.headers
    }
}
