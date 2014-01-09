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
package org.openehealth.ipf.tutorials.iti42.dataformat

import org.apache.camel.Exchange
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.*
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.lcm.*
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.query.*
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rs.*

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException


class XdsDataFormat implements org.apache.camel.spi.DataFormat {

    public static final JAXBContext JAXB_CONTEXT;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(
                    AdhocQueryRequest.class,
                    ProvideAndRegisterDocumentSetRequestType.class,
                    SubmitObjectsRequest.class,
                    RetrieveDocumentSetRequestType.class,
                    RemoveObjectsRequest.class,
                    RetrieveImagingDocumentSetRequestType.class,
                    AdhocQueryResponse.class,
                    RegistryResponseType.class,
                    RetrieveDocumentSetResponseType.class
            );
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
        JAXB_CONTEXT.createMarshaller().marshal(graph, stream)
    }

    @Override
    Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
        return JAXB_CONTEXT.createUnmarshaller().unmarshal(stream)
    }
}
