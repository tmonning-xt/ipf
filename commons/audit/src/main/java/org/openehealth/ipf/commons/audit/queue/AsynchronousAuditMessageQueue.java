/*
 * Copyright 2017 the original author or authors.
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

import lombok.NoArgsConstructor;
import org.openehealth.ipf.commons.audit.AuditContext;
import org.openehealth.ipf.commons.audit.AuditException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Audit queue that uses an injectable {@link ExecutorService} to asynchronously send away audit events.
 * When this queue is {@link #shutdown() shut down}, the executor service is also, waiting for at most
 * {@link #shutdownTimeoutSeconds} until all pending events are sent.
 * <p>
 * Note that the {@link ExecutorService} must be explicitly set, otherwise the implementation sends away the event synchronously.
 * The parameters of the {@link ExecutorService} determine the behavior of the queue implementation, e.g. in case the
 * audit destination is not reachable.
 * </p>
 *
 * @author Christian Ohr
 * @since 3.5
 */
@NoArgsConstructor
public class AsynchronousAuditMessageQueue extends AbstractAuditMessageQueue {

    private static final Logger LOG = LoggerFactory.getLogger(AsynchronousAuditMessageQueue.class);

    private ExecutorService executorService;
    private int shutdownTimeoutSeconds = 30;
    
    /**
     * Create a AsynchronousAuditMessageQueue with a 1 worker thread and a
     * automatic timeout handling to prevent thread is blocking endless.
     * 
     * @param asyncTimeoutoutSeconds The timeout after which the execution is
     *                               interrupted, Use "-1" to disable the timeout.
     */
    public AsynchronousAuditMessageQueue(int asyncTimeoutSeconds) {
        setExecutorService(new TimeoutThreadPoolExecutor(1, 1, Integer.MAX_VALUE, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), asyncTimeoutSeconds, TimeUnit.SECONDS));
    }

    /**
     * Sets the executor service. If this is null (or not used), audit events are sent synchronously
     *
     * @param executorService executor service
     */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Sets the timeout before the executor service closes. Defaults to 10
     *
     * @param shutdownTimeoutSeconds timeout before the executor service closes
     */
    public void setShutdownTimeoutSeconds(int shutdownTimeoutSeconds) {
        this.shutdownTimeoutSeconds = shutdownTimeoutSeconds;
    }

    @Override
    protected void handle(AuditContext auditContext, String auditRecord) {
        if (auditRecord != null) {
            var runnable = runnable(auditContext, auditRecord);
            if (executorService != null && !executorService.isShutdown()) {
                CompletableFuture.runAsync(runnable, executorService)
                        .exceptionally(e -> {
                            auditContext.getAuditExceptionHandler().handleException(auditContext, e, auditRecord);
                            return null;
                        });
            } else {
                runnable.run();
            }
        }
    }

    private Runnable runnable(AuditContext auditContext, String auditRecord) {
        // Copy the MDC contextMap to re-use it in the worker thread
        // See this recommendation here: http://logback.qos.ch/manual/mdc.html#managedThreads
        var mdcContextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                MDC.setContextMap(mdcContextMap);
                auditContext.getAuditTransmissionProtocol().send(auditContext, auditRecord);
            } catch (Exception e) {
                throw new AuditException(e);
            } finally {
                MDC.clear();
            }
        };
    }

    @Override
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(shutdownTimeoutSeconds, TimeUnit.SECONDS)) {
                    LOG.warn("Timeout occurred when flushing Audit events, some events might have been lost");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                LOG.warn("Thread interrupt when flushing ATNA events, some events might have been lost", e);
            }
        }
    }

}
