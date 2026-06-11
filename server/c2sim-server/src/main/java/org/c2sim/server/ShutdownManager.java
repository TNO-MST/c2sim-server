package org.c2sim.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handle shutdown of the server
 *
 * Options:
 * ShutdownManager.addShutdownListener(() -> {
 *     // do stuff
 * });
 * OR
 * while (!ShutdownManager.isShuttingDown()) {
 *     processNextTask();
 * }
 * OR
 * ShutdownManager.awaitShutdown();
 */
public final class ShutdownManager {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownManager.class);

    private static final AtomicBoolean shuttingDown =
            new AtomicBoolean(false);

    private static final CountDownLatch shutdownLatch =
            new CountDownLatch(1);

    private static final List<Runnable> listeners =
            new CopyOnWriteArrayList<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered. Application is shutting down...");

            if (shuttingDown.compareAndSet(false, true)) {
                shutdownLatch.countDown();

                listeners.forEach(listener -> {
                    try {
                        listener.run();
                    } catch (Exception e) {
                        // log
                    }
                });
            }
        }));
    }

    public static boolean isShuttingDown() {
        return shuttingDown.get();
    }

    public static void awaitShutdown() throws InterruptedException {
        shutdownLatch.await();
    }

    public static void addShutdownListener(Runnable listener) {
        listeners.add(listener);
    }
}
