/*
 * Copyright 2009 the original author or authors.
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
package org.openehealth.ipf.platform.camel.ihe.mllp.commons.consumer;

import static org.openehealth.ipf.platform.camel.core.util.Exchanges.resultMessage;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openehealth.ipf.modules.hl7.HL7v2Exception;
import org.openehealth.ipf.modules.hl7.message.MessageUtils;
import org.openehealth.ipf.modules.hl7dsl.MessageAdapter;
import org.openehealth.ipf.platform.camel.ihe.mllp.commons.MllpAdaptingException;
import org.openehealth.ipf.platform.camel.ihe.mllp.commons.MllpEndpoint;
import org.openehealth.ipf.platform.camel.ihe.mllp.commons.MllpMarshalUtils;
import org.openehealth.ipf.platform.camel.ihe.mllp.commons.MllpTransactionConfiguration;

import ca.uhn.hl7v2.model.Message;


/**
 * Consumer-side HL7 marshaling/unmarshaling Camel interceptor.
 * @author Dmytro Rud
 */
public class MllpConsumerMarshalInterceptor extends AbstractMllpConsumerInterceptor {
    private static final transient Log LOG = LogFactory.getLog(MllpConsumerMarshalInterceptor.class);

    public MllpConsumerMarshalInterceptor(MllpEndpoint endpoint, Processor wrappedProcessor) {
        super(endpoint, wrappedProcessor);
    }


    /**
     * Unmarshals the request, passes it to the processing route, 
     * and marshals the response.
     */
    public void process(Exchange exchange) throws Exception {
        String charset = getMllpEndpoint().getConfiguration().getCharsetName();
        MessageAdapter originalAdapter = null;
        
        // unmarshal
        boolean unmarshallingFailed = false;
        try {
            MllpMarshalUtils.unmarshal(exchange.getIn(), charset, getMllpEndpoint().getParser()); 
            originalAdapter = exchange.getIn().getBody(MessageAdapter.class).copy();
            exchange.getIn().setHeader(ORIGINAL_MESSAGE_HEADER_NAME, originalAdapter);
        } catch (Exception e) {
            unmarshallingFailed = true;
            LOG.error("Unmarshalling failed, message processing not possible", e);
            processUnmarshallingException(exchange, e);
        }
        
        // run the route
        if( ! unmarshallingFailed) {
            try {
                exchange.setProperty(Exchange.CHARSET_NAME, charset);
                getWrappedProcessor().process(exchange);
            } catch (MllpAdaptingException mae) {
                throw mae;
            } catch (Exception e) {
                LOG.error("Message processing failed", e);
                resultMessage(exchange).setBody(MllpMarshalUtils.createNak(
                        e, 
                        (Message) originalAdapter.getTarget(), 
                        getMllpEndpoint().getTransactionConfiguration()));
            }
        }
        
        // marshal 
        String s = MllpMarshalUtils.marshalStandardTypes(
                resultMessage(exchange), 
                charset, 
                getMllpEndpoint().getParser());
        resultMessage(exchange).setBody(s);
    }
    
    
    /**
     * Generates a default NAK message on unmarshalling errors 
     * and stores it into the exchange.
     */
    private void processUnmarshallingException(Exchange exchange, Throwable t) {
        MllpTransactionConfiguration config = getMllpEndpoint().getTransactionConfiguration();
        
        HL7v2Exception hl7e = new HL7v2Exception(
                MllpMarshalUtils.formatErrorMessage(t),
                config.getRequestErrorDefaultErrorCode(), 
                t);
        
        Object nak = MessageUtils.defaultNak(
                hl7e, 
                config.getRequestErrorDefaultAckTypeCode(), 
                config.getHl7Version(),
                config.getSendingApplication(),
                config.getSendingFacility());
        
        resultMessage(exchange).setBody(nak);
    }

}
