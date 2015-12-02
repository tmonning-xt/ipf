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
package org.openehealth.ipf.platform.camel.ihe.xds.iti16;

import org.apache.camel.Endpoint;
import org.openehealth.ipf.commons.ihe.core.atna.AuditStrategy;
import org.openehealth.ipf.commons.ihe.ws.JaxWsClientFactory;
import org.openehealth.ipf.commons.ihe.ws.WsTransactionConfiguration;
import org.openehealth.ipf.commons.ihe.xds.core.audit.XdsQueryAuditDataset;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs21.query.AdhocQueryRequest;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs21.rs.RegistryResponse;
import org.openehealth.ipf.commons.ihe.xds.iti16.Iti16ClientAuditStrategy;
import org.openehealth.ipf.commons.ihe.xds.iti16.Iti16PortType;
import org.openehealth.ipf.commons.ihe.xds.iti16.Iti16ServerAuditStrategy;
import org.openehealth.ipf.platform.camel.ihe.ws.AbstractWsEndpoint;
import org.openehealth.ipf.platform.camel.ihe.ws.AbstractWsProducer;
import org.openehealth.ipf.platform.camel.ihe.ws.SimpleWsProducer;
import org.openehealth.ipf.platform.camel.ihe.xds.XdsComponent;
import org.openehealth.ipf.platform.camel.ihe.xds.XdsEndpoint;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * The Camel component for the ITI-16 transaction.
 */
public class Iti16Component extends XdsComponent<XdsQueryAuditDataset> {

    private final static WsTransactionConfiguration WS_CONFIG = new WsTransactionConfiguration(
            new QName("urn:ihe:iti:xds:2007", "DocumentRegistry_Service", "ihe"),
            Iti16PortType.class,
            new QName("urn:ihe:iti:xds:2007", "DocumentRegistry_Binding_Soap11", "ihe"),
            false,
            "wsdl/iti16.wsdl",
            false,
            false,
            true,
            false);

    @Override
    @SuppressWarnings({"raw", "unchecked"}) // Required because of base class
    protected Endpoint createEndpoint(String uri, String remaining, Map parameters) throws Exception {
        return new XdsEndpoint<XdsQueryAuditDataset>(uri, remaining, this,
                getCustomInterceptors(parameters),
                getFeatures(parameters),
                getSchemaLocations(parameters),
                getProperties(parameters),
                Iti16Service.class) {
            @Override
            public AbstractWsProducer<XdsQueryAuditDataset, WsTransactionConfiguration, ?, ?> getProducer(AbstractWsEndpoint<XdsQueryAuditDataset, WsTransactionConfiguration> endpoint,
                                                  JaxWsClientFactory<XdsQueryAuditDataset> clientFactory) {
                return new SimpleWsProducer<>(
                        endpoint, clientFactory, AdhocQueryRequest.class, RegistryResponse.class);
            }
        };
    }

    @Override
    public WsTransactionConfiguration getWsTransactionConfiguration() {
        return WS_CONFIG;
    }

    @Override
    public AuditStrategy<XdsQueryAuditDataset> getClientAuditStrategy() {
        return new Iti16ClientAuditStrategy();
    }

    @Override
    public AuditStrategy<XdsQueryAuditDataset> getServerAuditStrategy() {
        return new Iti16ServerAuditStrategy();
    }

}
