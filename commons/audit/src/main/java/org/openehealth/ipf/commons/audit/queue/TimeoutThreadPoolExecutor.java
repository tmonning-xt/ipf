package org.openehealth.ipf.commons.audit.queue;

import org.openehealth.ipf.commons.audit.protocol.TLSSyslogSenderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the {@link ExecutorService} that fits best when using the
 * {@link AsynchronousAuditMessageQueue} with a blocking transport (like
 * {@link TLSSyslogSenderImpl}.
 * 
 * The executor will use a timeout handling to ensure no execution will block
 * endless.
 * 
 * @since 4.4
 */
public class TimeoutThreadPoolExecutor extends ThreadPoolExecutor {
    private final long timeout;
    private final TimeUnit timeoutUnit;
    private static final Logger LOG = LoggerFactory.getLogger(TimeoutThreadPoolExecutor.class);

    private final ScheduledExecutorService timeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentMap<Runnable, ScheduledFuture<?>> runningTasks = new ConcurrentHashMap<Runnable, ScheduledFuture<?>>();

    /**
     * Initialize a timeout threadpoolexecutor.
     * 
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     * @param timeout the timeout after which the current worker thread is interrupted.
     * @param timeoutUnit the time unit for the {@code timeout} argument
     */
    public TimeoutThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, long timeout, TimeUnit timeoutUnit) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
    }

    @Override
    public void shutdown() {
        timeoutExecutor.shutdown();
        super.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        timeoutExecutor.shutdownNow();
        return super.shutdownNow();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        LOG.debug("Prepare execution, current queueSize [{}]", getQueue().size());
        if (timeout > 0) {
            final ScheduledFuture<?> scheduled = timeoutExecutor.schedule(new TimeoutTask(t), timeout, timeoutUnit);
            runningTasks.put(r, scheduled);
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        ScheduledFuture<?> timeoutTask = runningTasks.remove(r);
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
        }
    }

    class TimeoutTask implements Runnable {
        private final Thread thread;

        public TimeoutTask(Thread thread) {
            this.thread = thread;
        }

        @Override
        public void run() {
            LOG.error("Abort execution, because timeout of {} {} was reached.", timeout, timeoutUnit);
            thread.interrupt();
        }
    }
}