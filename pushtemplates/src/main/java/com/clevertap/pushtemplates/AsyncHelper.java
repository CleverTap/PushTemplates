package com.clevertap.pushtemplates;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class AsyncHelper {

    private long EXECUTOR_THREAD_ID = 0;
    private ExecutorService es;

    private static AsyncHelper asyncHelperInstance = null;

    private AsyncHelper() {
        this.es = Executors.newFixedThreadPool(1);
    }

    static AsyncHelper getInstance() {
        if (asyncHelperInstance == null) {
            asyncHelperInstance = new AsyncHelper();
        }
        return asyncHelperInstance;
    }

    @SuppressWarnings("UnusedParameters")
    void postAsyncSafely(final String name, final Runnable runnable) {
        try {
            final boolean executeSync = Thread.currentThread().getId() == EXECUTOR_THREAD_ID;

            if (executeSync) {
                runnable.run();
            } else {
                this.es.submit(new Runnable() {
                    @Override
                    public void run() {
                        EXECUTOR_THREAD_ID = Thread.currentThread().getId();
                        try {
                            runnable.run();
                        } catch (Throwable t) {
                            PTLog.error("Executor service: Failed to complete the scheduled task");
                        }
                    }
                });
            }
        } catch (Throwable t) {
            PTLog.error("Failed to submit task to the executor service");
        }
    }
}
