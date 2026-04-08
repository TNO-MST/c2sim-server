package org.c2sim.client.websockets;

import java.nio.CharBuffer;
import java.util.function.Consumer;

/**
 * Stateful line splitter for a continuous character stream.
 *
 * <p>Designed for chunked WebSocket text frames: each call to {@link #accept(String)} appends the
 * chunk to an internal carry buffer and emits one {@link CharSequence} per complete line
 * (terminated by {@code '\n'} or {@code "\r\n"}) to the configured {@code sink}.
 *
 * <p>Zero-copy optimisation: when a complete line fits within a single chunk and the carry buffer
 * is empty a {@link CharBuffer} view of the original string is passed to the sink instead of
 * copying characters.
 *
 * <p>Call {@link #flush()} to emit any remaining partial line when the stream ends.
 */
public final class StreamMsgSplitter {
  private final StringBuilder carry = new StringBuilder(1024);
  private final Consumer<CharSequence> sink;

  /**
   * Creates a splitter that forwards complete lines to the given sink.
   *
   * @param sink the consumer invoked for each complete line (without the line terminator)
   */
  public StreamMsgSplitter(Consumer<CharSequence> sink) {
    this.sink = sink;
  }

  /**
   * Feeds the next chunk of text into the splitter.
   *
   * <p>All complete lines contained in {@code chunk} (including those spanning the boundary with
   * the previous chunk) are immediately forwarded to the sink.
   *
   * @param chunk the next text chunk received from the stream
   */
  public void accept(String chunk) {
    int from = 0;
    final int n = chunk.length();

    for (int i = 0; i < n; i++) {
      char ch = chunk.charAt(i);
      if (ch == '\n') {
        // Trim optional '\r' (Windows CRLF)
        int lineEnd = (i > from && chunk.charAt(i - 1) == '\r') ? i - 1 : i;

        if (carry.isEmpty()) {
          // zero-copy: view into this chunk only
          CharBuffer buf = CharBuffer.wrap(chunk, from, lineEnd);

          boolean isBlank = true;
          for (int index = buf.position(); index < buf.limit(); index++) {
            if (!Character.isWhitespace(buf.get(index))) {
              isBlank = false;
              break;
            }
          }

          if (!isBlank) {
            sink.accept(buf);
          }

        } else {
          // crosses chunk boundary: minimal copy (only this line)
          carry.append(chunk, from, lineEnd);
          var line = carry.toString();
          if (!line.isBlank()) {
            sink.accept(line);
          }
          carry.setLength(0);
        }
        from = i + 1; // next segment starts after '\n'
      }
    }

    // Remainder (partial line) — keep it for the next chunk
    if (from < n) {
      carry.append(chunk, from, n);
    }
  }

  /**
   * Emits the remaining partial line (if any) to the sink and clears the carry buffer.
   *
   * <p>Should be called when the stream ends to avoid silently dropping an unterminated last line.
   */
  public void flush() {
    if (!carry.isEmpty()) {
      sink.accept(carry.toString());
      carry.setLength(0);
    }
  }
}
