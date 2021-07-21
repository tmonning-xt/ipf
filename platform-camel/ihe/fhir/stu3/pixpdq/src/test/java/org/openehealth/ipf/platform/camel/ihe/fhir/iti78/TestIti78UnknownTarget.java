/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openehealth.ipf.platform.camel.ihe.fhir.iti78;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 */
public class TestIti78UnknownTarget extends AbstractTestIti78 {

    private static final String CONTEXT_DESCRIPTOR = "iti-78-unknown-target.xml";

    @BeforeAll
    public static void setUpClass() throws ServletException {
        startServer(CONTEXT_DESCRIPTOR, false);
        startClient();
    }

    @Test
    public void testSendManualPixm() {
        assertThrows(ResourceNotFoundException.class, ()-> {
            try {
                sendManually(familyParameters());
            } catch (ResourceNotFoundException e) {
                assertAndRethrow(e, OperationOutcome.IssueType.VALUE);
            }
        });
    }

}
