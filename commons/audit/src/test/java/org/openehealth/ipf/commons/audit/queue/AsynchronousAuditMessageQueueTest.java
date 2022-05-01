/*
 * Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openehealth.ipf.commons.audit.queue;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openehealth.ipf.commons.audit.AuditContext;
import org.openehealth.ipf.commons.audit.AuditException;
import org.openehealth.ipf.commons.audit.DefaultAuditContext;
import org.openehealth.ipf.commons.audit.handler.AuditExceptionHandler;
import org.openehealth.ipf.commons.audit.marshal.dicom.Current;
import org.openehealth.ipf.commons.audit.model.AuditMessage;
import org.openehealth.ipf.commons.audit.protocol.AuditTransmissionProtocol;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 *
 */
public class AsynchronousAuditMessageQueueTest {
    
    @Test
    public void sendMessageWithoutExecutor() throws Exception {
        final var messageSender = mock(AuditTransmissionProtocol.class);
        final var context = new DefaultAuditContext();
        context.setAuditEnabled(true);
        var queue = new AsynchronousAuditMessageQueue();
        context.setAuditMessageQueue(queue);
        context.setAuditTransmissionProtocol(messageSender);

        var auditMessage = someAuditEventMessage();
        context.audit(auditMessage);

        verify(messageSender).send(context, Current.toString(auditMessage, false));
        verifyNoMoreInteractions(messageSender);
    }

    @Test
    public void sendMessageWithExecutor() throws Exception {
        final var messageSender = mock(AuditTransmissionProtocol.class);
        final var context = new DefaultAuditContext();
        context.setAuditEnabled(true);
        var queue = new AsynchronousAuditMessageQueue();
        context.setAuditMessageQueue(queue);
        context.setAuditTransmissionProtocol(messageSender);
        try {
            queue.setExecutorService(Executors.newSingleThreadExecutor());
            var auditMessage = someAuditEventMessage();
            context.audit(auditMessage);

            verify(messageSender, timeout(500).times(1)).send(context, Current.toString(auditMessage, false));
            verifyNoMoreInteractions(messageSender);
        } finally {
            queue.shutdown();
        }
    }
    
    @Test
    public void sendMessageWithTimeout() throws Exception {
        final var messageSender = mock(AuditTransmissionProtocol.class);
        final var context = new DefaultAuditContext();
        context.setAuditEnabled(true);
        var queue = new AsynchronousAuditMessageQueue(2);
        context.setAuditMessageQueue(queue);
        AuditExceptionHandler captureFailure = mock(AuditExceptionHandler.class);
        ArgumentCaptor<Exception> valueCapture = ArgumentCaptor.forClass(Exception.class);
        Mockito.doNothing().when(captureFailure).handleException(Mockito.any(AuditContext.class),
                valueCapture.capture(), Mockito.anyString());
        context.setAuditExceptionHandler(captureFailure);
        context.setAuditTransmissionProtocol(messageSender);
        try {
            var auditMessage = someAuditEventMessage();
            String auditString = Current.toString(auditMessage, false);
            doAnswer((invocation) -> {
                TimeUnit.SECONDS.sleep(5);
                return null;
            }).when(messageSender).send(context, auditString);
            context.audit(auditMessage);

            verify(captureFailure, timeout(4000).times(1)).handleException(Mockito.eq(context),
                    Mockito.any(Exception.class), Mockito.eq(auditString));
            assertEquals(AuditException.class, valueCapture.getValue().getCause().getClass());
        } finally {
            queue.shutdown();
        }
    }

    private AuditMessage someAuditEventMessage() {
        return mock(AuditMessage.class);
    }

}
