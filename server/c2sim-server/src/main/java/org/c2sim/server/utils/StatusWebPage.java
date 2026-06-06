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
                   <th>ID</th>
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

        long sec = client.getPartiallyConnectedInSeconds();
        var handshake = sec >= 0 ? String.format("%d sec", sec) : "Complete";
        html.append(
            String.format(
                "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td style=\"text-align:center\">%s</td>"
                    + "<td style=\"text-align:center\">%s</td><td>%s</td><td>%s</td></tr>%n",
                StringEscapeUtils.escapeHtml4(displayName),
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

    html.append("""
            <button onclick="cleanupAndRefresh()">Cleanup</button>
            
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
