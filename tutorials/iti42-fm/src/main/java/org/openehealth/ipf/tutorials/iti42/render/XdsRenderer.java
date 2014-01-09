/*
 * Copyright 2008 the original author or authors.
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
package org.openehealth.ipf.tutorials.iti42.render;

import org.openehealth.ipf.platform.camel.flow.PlatformMessage;
import org.openehealth.ipf.platform.camel.flow.PlatformMessageRenderer;
import org.openehealth.ipf.tutorials.iti42.dataformat.XdsDataFormat;

import java.io.StringWriter;

public class XdsRenderer implements PlatformMessageRenderer {

    @Override
    public String render(PlatformMessage message) {
        StringWriter writer = new StringWriter();
        String result = "";
        try {
            XdsDataFormat.JAXB_CONTEXT.createMarshaller().marshal(message.getExchange().getIn().getBody(), writer);
            result = writer.toString();
        } catch (Exception e){
            result = message.getExchange().getIn().getBody(String.class);
        }
        return result;
    }
    
}
