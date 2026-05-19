package org.c2sim.client.helpers;

import org.c2sim.client.exceptions.C2ClientException;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.invoker.ApiException;
import org.slf4j.Logger;

import java.net.ConnectException;
import java.util.Objects;

public class LoggingHelper {
    // Prevent instantiation
    private LoggingHelper() {
        throw new AssertionError("Only static functions");
    }


    /**
     * Reduce logging and not throw full stack trace for common errors
     * <p>
     * Supported exception types are:
     * <ul>
     *   <li>{@code C2SimRestException}</li>
     *   <li>{@code ApiException}</li>
     *   <li>{@code C2ClientException}</li>
     * </ul>
     * Any other exception type is logged as a generic exception.
     * @param logger
     *      the logger used to write the error message
     * @param exception
     *      the exception to log
     * @param message
     *      the contextual message describing the operation that failed
     *
     * @throws NullPointerException
     *      if {@code logger}, {@code exception}, or {@code message} is {@code null}
     */
    public static void logRestException(
            final Logger logger,
            final Exception exception,
            final String message) {
        Objects.requireNonNull(logger);
        Objects.requireNonNull(exception);
        Objects.requireNonNull(message);
        if (exception instanceof C2SimRestException ex) {
            String errMsg = String.format("%s [C2SimRestException -> %s]",
                    message, ex.getMessage());
            logger.error(errMsg);

        } else if (exception instanceof ApiException ex) {
            var error = String.format("%s [ApiException -> %s]",
                    message, ex.getCause().getMessage());
            if (ex.getCause() instanceof ConnectException) {
                logger.error(error); // don't need stacktrace
            } else {
                logger.error(error, exception);
            }

        } else if (exception instanceof C2ClientException ex) {
            String errMsg = String.format("%s [C2ClientException -> %s]",
                    message, ex.getMessage());
            logger.error(errMsg);
        } else {
            String errMsg = String.format("%s [Exception -> %s]",
                    message, exception.getMessage());
            logger.error(errMsg);
        }

    }


}
