package org.c2sim.lox.validation;

import java.io.InputStream;
import java.io.Reader;
import org.w3c.dom.ls.LSInput;

/**
 * Minimal {@link LSInput} implementation used by {@link ResourceResolver} to supply XSD schema
 * streams to the JAXP {@link javax.xml.validation.SchemaFactory}.
 *
 * <p>Only {@link #getByteStream()} / {@link #setByteStream(InputStream)}, {@link #getPublicId()} /
 * {@link #setPublicId(String)}, and {@link #getSystemId()} / {@link #setSystemId(String)} are
 * implemented. All other accessors return {@code null}, {@code false}, or do nothing.
 */
public class LSInputImpl implements LSInput {

  private String publicId;
  private String systemId;
  private InputStream inputStream;

  /**
   * Creates a new {@link LSInputImpl} with the given identifiers and byte stream.
   *
   * @param publicId the public identifier of the resource, or {@code null}
   * @param systemId the system identifier (URI) of the resource, or {@code null}
   * @param inputStream the byte stream providing the resource content
   */
  public LSInputImpl(String publicId, String systemId, InputStream inputStream) {
    this.publicId = publicId;
    this.systemId = systemId;
    this.inputStream = inputStream;
  }

  /**
   * @return {@code null} — not used; see {@link #getByteStream()}
   */
  @Override
  public Reader getCharacterStream() {
    return null; // Not used; see getByteStream
  }

  /** Not implemented. */
  @Override
  public void setCharacterStream(Reader characterStream) {
    // Not implemented
  }

  /** {@inheritDoc} */
  @Override
  public InputStream getByteStream() {
    return this.inputStream;
  }

  /** {@inheritDoc} */
  @Override
  public void setByteStream(InputStream byteStream) {
    this.inputStream = byteStream;
  }

  /**
   * @return {@code null} — not used; see {@link #getByteStream()}
   */
  @Override
  public String getStringData() {
    return null; // Not used; see getByteStream
  }

  /** Not implemented. */
  @Override
  public void setStringData(String stringData) {
    // Not implemented
  }

  /** {@inheritDoc} */
  @Override
  public String getSystemId() {
    return this.systemId;
  }

  /** {@inheritDoc} */
  @Override
  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  /** {@inheritDoc} */
  @Override
  public String getPublicId() {
    return this.publicId;
  }

  /** {@inheritDoc} */
  @Override
  public void setPublicId(String publicId) {
    this.publicId = publicId;
  }

  /**
   * @return {@code null}
   */
  @Override
  public String getBaseURI() {
    return null;
  }

  /** Not implemented. */
  @Override
  public void setBaseURI(String baseURI) {
    // Not implemented
  }

  /**
   * @return {@code null}
   */
  @Override
  public String getEncoding() {
    return null;
  }

  /** Not implemented. */
  @Override
  public void setEncoding(String encoding) {
    // Not implemented
  }

  /**
   * @return {@code false}
   */
  @Override
  public boolean getCertifiedText() {
    return false;
  }

  /** Not implemented. */
  @Override
  public void setCertifiedText(boolean certifiedText) {
    // Not implemented
  }
}
