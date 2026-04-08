package org.c2sim.server.rest.impl;

import static org.c2sim.server.utils.ContextHelper.ATTRIB_SHARED_SESSION_ID;
import static org.c2sim.server.utils.ContextHelper.ATTRIB_TRACKING_ID;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.c2sim.lox.sax.ExtractC2SimHeader;
import org.c2sim.server.api.apis.PublishApiService;
import org.c2sim.server.api.models.ResponseSend;
import org.c2sim.server.exceptions.C2SimException;
import org.c2sim.server.services.C2SimService;
import org.c2sim.server.services.ConfigService;
import org.c2sim.server.services.MetricService;
import org.c2sim.server.utils.ContextHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PublishApiService} implementation that handles C2SIM document publish requests.
 *
 * <p>Accepts documents either as raw request-body text ({@code text/xml}) or as a {@code
 * multipart/form-data} file upload. Before forwarding the document to the C2SIM service the size is
 * checked against the configured maximum (see {@link ConfigService#getMaxC2SimMessageSizeInMb()}).
 */
public class PublishApiServiceImpl implements PublishApiService {
  private static final Logger logger = LoggerFactory.getLogger(PublishApiServiceImpl.class);

  private final C2SimService c2simService;
  private final ConfigService configService;
  private final MetricService metricService;

  /**
   * Creates the service.
   *
   * @param c2simService the C2SIM service that processes published documents
   * @param configService the configuration service used to enforce the message-size limit
   */
  public PublishApiServiceImpl(
      C2SimService c2simService, ConfigService configService, MetricService metricService) {
    this.c2simService = c2simService;
    this.configService = configService;
    this.metricService = metricService;
    logger.debug("PublishApiServiceImpl created....");
  }

  private static final float MEGA_BYTE = 1024f * 1024f;

  /**
   * {@inheritDoc}
   *
   * <p>Reads the C2SIM XML document from the request (multipart or raw body), enforces the
   * configured size limit, and delegates to {@link C2SimService#publishC2SimDoc}.
   *
   * @throws C2SimException with {@link C2SimException.ErrorCode#C2SIM_MSG_SIZE_EXCEEDED} if the
   *     document exceeds the configured size limit
   * @throws C2SimException with {@link C2SimException.ErrorCode#IO_ERROR} if closing the stream
   *     fails
   */
  @SuppressWarnings("squid:S125") // Allow commented out source code
  @NotNull
  @Override
  public ResponseSend send(
      @NotNull String clientId,
      @NotNull String sessionId,
      UploadedFile bodyAsFile,
      @NotNull Context ctx) {
    ctx.attribute(ATTRIB_SHARED_SESSION_ID, sessionId);

    String trackingId = ContextHelper.getAttributeValue(ctx, ATTRIB_TRACKING_ID);

    // clientId is already validated by global handler
    float fileSizeInMb = 0;
    // Is the request body (content type) plain text or multipart/form-data ?

    byte[] bodyBytes;
    try {
      if (bodyAsFile != null) {
        try (InputStream in = bodyAsFile.content()) {
          bodyBytes = in.readAllBytes();
        }
      } else {
        bodyBytes = ctx.bodyAsBytes();
      }
    } catch (IOException e) {
      throw new C2SimException(
          C2SimException.ErrorCode.IO_ERROR, "Failed to read RESTful message body.");
    }

    // String contentLength = ctx.header("Content-Length");
    // long bodySize = contentLength != null ? Long.parseLong(contentLength) : -1;
    // It for client not mandatory to set this header field
    fileSizeInMb = bodyBytes.length / MEGA_BYTE;

    // Need C2SIM header for metrics (the sender system), see failed requests
    // The C2SIM header is also extracted in C2SIM service
    // So is now done twice, maybe improve in future
    var header = ExtractC2SimHeader.extract(new ByteArrayInputStream(bodyBytes));

    // Store metric info c2sim message size
    // TODO use auth header is exist?
    var systemName = header != null ? header.getFromSendingSystem() : "UNKNOWN";
    metricService.incBytesSendByC2SimClient(sessionId, systemName, bodyBytes.length);

    if (header == null) {
      throw new C2SimException(
          C2SimException.ErrorCode.C2SIM_INVALID_HEADER,
          "Could not extract C2SIM header from C2SIM message.");
    }

    // Check file size of C2SIM message (prevent wasting CPU cycles)
    if (fileSizeInMb > configService.getMaxC2SimMessageSizeInMb()) {
      throw new C2SimException(
          C2SimException.ErrorCode.C2SIM_MSG_SIZE_EXCEEDED,
          String.format(
              "C2SIM messages exceeds %.2f MB limit (size is %.2f MB)  ",
              configService.getMaxC2SimMessageSizeInMb(), fileSizeInMb));
    }

    // Store for later use in metric service (if needed)
    ctx.attribute(ContextHelper.ATTRIB_C2SIM_HEADER, header);

    // C2SIM service can now process the XML C2SIM message
    try (InputStream autoClose = new ByteArrayInputStream(bodyBytes)) {
      c2simService.publishC2SimDoc(
          sessionId,
          clientId,
          trackingId,
          autoClose,
          ctx.attribute(ContextHelper.ATTRIB_AUTHORIZER));
      return new ResponseSend(trackingId);
    } catch (IOException e) {
      throw new C2SimException(C2SimException.ErrorCode.IO_ERROR, "Failed closing publish");
    }
  }
}
