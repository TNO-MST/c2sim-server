package org.c2sim.client.helpers;

import java.io.IOException;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.invoker.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for executing C2SIM REST API calls with structured error handling.
 *
 * <p>HTTP 400 responses are intercepted, their body deserialized into a {@link
 * org.c2sim.client.model.C2SimError}, and re-thrown as a {@link C2SimRestException}. All other
 * {@link ApiException}s are re-thrown unchanged.
 */
public final class ExceptionHelper {
  private static final Logger logger = LoggerFactory.getLogger(ExceptionHelper.class);

  /** Exception handling for REST calls */
  public ExceptionHelper() {
    super();
  }

  /**
   * A functional interface for a supplier that may throw an {@link ApiException}.
   *
   * @param <T> the type of the supplied value
   */
  @FunctionalInterface
  public interface ThrowingSupplier<T> {
    /**
     * Returns a value, potentially throwing an {@link ApiException}.
     *
     * @return the computed value
     * @throws ApiException if the underlying API call fails
     */
    T get() throws ApiException;
  }

  /**
   * Executes the given supplier, converting HTTP 400 {@link ApiException}s into {@link
   * C2SimRestException}s with a parsed error body.
   *
   * @param <T> the return type
   * @param supplier the API call to execute
   * @return the value produced by the supplier
   * @throws ApiException if the API call fails with a status other than 400, or if the 400 body
   *     cannot be parsed
   * @throws C2SimRestException if the API call fails with HTTP 400 and the body is a valid {@link
   *     org.c2sim.client.model.C2SimError}
   */
  public static <T> T executeWithExceptionWrapping(ThrowingSupplier<T> supplier)
      throws ApiException, C2SimRestException {
    try {
      return supplier.get();
    } catch (ApiException ex) {
      // Error code 400 is Error message body
      if (ex.getCode() == 400) {
        try {
          var error = org.c2sim.client.model.C2SimError.fromJson(ex.getResponseBody());
          throw new C2SimRestException(ex, error);
        } catch (IOException e) {
          logger.error("Expected in REST message body type Error, failed to deserialize ", e);
          // Failed to parse message body, just throw ApiException
        }
      }
      throw ex;
    }
  }
}
