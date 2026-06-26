package org.c2sim.server.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.text.StringEscapeUtils;
import org.c2sim.server.services.C2SimService;
import org.c2sim.server.sessions.SharedSessionClient;

/**
 * Generates the HTML status page served at {@code GET /status}.
 *
 * <p>The page lists every active shared session with its current state and a table of all connected
 * clients, including their join and streaming status. Build time (render duration) and render
 * timestamp are appended in the footer.
 *
 * <p>This is a utility class; instantiation is not allowed.
 */
public class StatusWebPage {

  private StatusWebPage() {
    throw new AssertionError("Only static functions");
  }

  private static String statusLed(boolean value) {
    return value
            ? "<span style=\"display:inline-block;"
            + "width:12px;"
            + "height:12px;"
            + "border-radius:50%;"
            + "background:#28a745\"></span>"
            : "<span style=\"display:inline-block;"
            + "width:12px;"
            + "height:12px;"
            + "border-radius:50%;"
            + "background:#dc3545\"></span>";
  }
  /**
   * Generates a complete HTML document describing the current server state.
   *
   * @param service the {@link C2SimService} used to enumerate sessions and clients
   * @return the HTML content as a string
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static String createStatusPage(C2SimService service) {
    boolean hasInvalidClientIds = false;
    long startTime = System.nanoTime();
    String renderTime =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    StringBuilder html = new StringBuilder();
    html.append(
        """
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <meta charset="UTF-8">
            <title>C2SIM Server</title>
            <style>
              body {font-family: Arial, sans-serif; background:#f8f9fa; margin:40px;}
              table {
                 border-collapse: collapse;
                 width: 60%;
                 margin-left: 0;
                 margin-right: auto;
                 background: white;
                 box-shadow: 0 0 10px rgba(0,0,0,0.1);
               }
              th, td {border:1px solid #ddd; padding:10px 15px; text-align:left;}
              th {background:#007BFF; color:white;}
              tr:nth-child(even) {background:#f2f2f2;}
              caption {
                 font-size: 1.3em;
                 margin-bottom: 10px;
                 font-weight: bold;
                 text-align: left;
                 caption-side: top; /* optional, ensures caption stays above */
               }
               .info-box {
                 width: 60%;
                 background: #ffffff;
                 border: 1px solid #ddd;
                 padding: 10px 15px;
                 margin-bottom: 15px;
                 box-shadow: 0 0 5px rgba(0,0,0,0.05);
               }
               .info-box .label {
                 font-weight: bold;
                 color: #007BFF;
                 display: inline-block;
                 width: 80px;
               }
               .error {
                  color: red;
                  font-weight: bold;
               }
               .no-error {
               }
            </style>
            </head>
            <body>""");

    for (var session : service.getSharedSessionManager()) {
      html.append(String.format("<h1>Shared Session '%s'</h1>", session.getSharedSessionName()));

      html.append(
          String.format(
              """
    <div class='info-box'>
      <div><span class='label'>Schema:</span> %s</div>
      <div><span class='label'>State:</span> %s</div>
    </div>
    """,
              session.getSchemaVersion(), session.getCurrentState()));
      html.append(
          """
                <table>
                   <caption>Connected clients</caption>
                   <tr>
                   <th>Display Name</th>
                   <th>clientId</th>
                   <th>IP address</th>
                   <th>AZP (auth)</th>
                   <th>System name</th>
                   <th>Joined</th>
                   <th>Streaming</th>
                   <th>Handshake</th>
                   <th>Lifetime (minutes)
                   </th></tr>""");
      for (SharedSessionClient client : session.getClientsManager()) {
        String waiting = "<waiting for join>";
        boolean joined = client.hasJoinedSharedSession();
        var displayName = joined ? client.getClientIdDisplayName() : waiting;
        var ipAddress = joined ? client.getClientIpAddress() : waiting;
        var azp = joined ? client.getAzp() : waiting;
        var systemName = joined ? client.getSystemName() : waiting;

        // Check
        var wrongClientId = (joined && systemName.toLowerCase().contains(client.getClientId().toLowerCase()));
        if (wrongClientId) {
          hasInvalidClientIds = true;
        }
        var className = wrongClientId ? "error" : "no_error";

        long sec = client.getPartiallyConnectedInSeconds();
        var handshake = sec >= 0 ? String.format("%d sec", sec) : "Complete";
        html.append(
            String.format(
                "<tr><td>%s</td><td class=\"%s\">%s</td><td>%s</td><td>%s</td><td>%s</td><td style=\"text-align:center\">%s</td>"
                    + "<td style=\"text-align:center\">%s</td><td>%s</td><td>%s</td></tr>%n",
                StringEscapeUtils.escapeHtml4(displayName),
                className,
                StringEscapeUtils.escapeHtml4(client.getClientId()),
                StringEscapeUtils.escapeHtml4(ipAddress),
                StringEscapeUtils.escapeHtml4(azp),
                StringEscapeUtils.escapeHtml4(systemName),
                statusLed(client.hasJoinedSharedSession()),
                statusLed(client.hasStreamToClient()),
                handshake,
                client.getCreationLifetimeInMinutes()));
      }
      html.append(
          """
                </table>
                
                """);
    }

    if (hasInvalidClientIds) {
      var addWarning = """
              <div style="
                  padding: 10px 14px;
                  margin: 10px 0;
                  border-left: 4px solid #f0ad4e;
                  background-color: #fff8e5;
                  color: #664d03;
                  font-size: 0.9rem;
              ">
                  <strong>Warning:</strong>
                  When C2SIM client is instantiated it must generate a random <code>clientId</code> (preferably 5 characters).
                  This <code>clientId</code> must be added to each REST header. Each C2SIM client <b>instance</b> should use its
                  own random <code>clientId</code>. The C2SIM server will reject non random clientId in the future.
              </div>
      """;
      html.append(addWarning);
    }

    html.append("""
            <button onclick="cleanupAndRefresh()" title="Remove dead (timeout) connections.">Cleanup</button>
            
            <script>
            async function cleanupAndRefresh() {
              await fetch('cleanup');
              location.reload();
            }
            </script>
            """);

    long endTime = System.nanoTime(); // stop timing
    double buildTimeMs = (endTime - startTime) / 1_000_000.0;

    html.append("<br><footer>")
        .append("Page rendered on: ")
        .append(renderTime)
        .append(String.format(" (Build time: %.2f ms)", buildTimeMs))
        .append(
            """
                      </footer>
            </body>
            </html>
            """);

    return html.toString();
  }
}
