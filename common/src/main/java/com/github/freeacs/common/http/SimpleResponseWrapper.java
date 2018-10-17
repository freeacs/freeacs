package com.github.freeacs.common.http;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class SimpleResponseWrapper implements HttpServletResponse {
  private final StringWriter stringResponse;
  private final HashMap<String, Object> headers;
  private final FileResponse fileResponse;

  private int status;
  private String contentType;
  private long contentLength;

  public SimpleResponseWrapper(int sc, String contentType) {
    this.status = sc;
    this.contentType = contentType;
    this.stringResponse = new StringWriter();
    this.fileResponse = new FileResponse();
    this.headers = new HashMap<>();
  }

  @Override
  public void addCookie(Cookie cookie) {
    // Not implemented
  }

  @Override
  public boolean containsHeader(String name) {
    return false;
  }

  @Override
  public String encodeURL(String url) {
    return null;
  }

  @Override
  public String encodeRedirectURL(String url) {
    return null;
  }

  @Override
  public String encodeUrl(String url) {
    return null;
  }

  @Override
  public String encodeRedirectUrl(String url) {
    return null;
  }

  @Override
  public void sendError(int sc, String msg) {
    this.status = sc;
    PrintWriter writer = getWriter();
    writer.print(msg);
    writer.close();
  }

  @Override
  public void sendError(int sc) {
    this.status = sc;
  }

  @Override
  public void sendRedirect(String location) {
    // Not implemented
  }

  @Override
  public void setDateHeader(String name, long date) {
    // Not implemented
  }

  @Override
  public void addDateHeader(String name, long date) {
    // Not implemented
  }

  @Override
  public void setHeader(String name, String value) {
    headers.put(name, value);
  }

  @Override
  public void addHeader(String name, String value) {
    headers.put(name, value);
  }

  @Override
  public void setIntHeader(String name, int value) {
    headers.put(name, value);
  }

  @Override
  public void addIntHeader(String name, int value) {
    headers.put(name, value);
  }

  public void setStatus(int sc) {
    this.status = sc;
  }

  @Override
  public void setStatus(int sc, String sm) {
    this.status = sc;
    getWriter().append(sm);
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public String getHeader(String name) {
    return null;
  }

  @Override
  public Collection<String> getHeaders(String name) {
    return null;
  }

  @Override
  public Collection<String> getHeaderNames() {
    return null;
  }

  @Override
  public String getCharacterEncoding() {
    return null;
  }

  @Override
  public String getContentType() {
    return this.contentType;
  }

  @Override
  public ServletOutputStream getOutputStream() {
    return fileResponse;
  }

  @Override
  public PrintWriter getWriter() {
    return new PrintWriter(stringResponse);
  }

  public byte[] getResponseAsBytes() {
    String stringResponse = this.stringResponse.toString();
    if (stringResponse.isEmpty()) {
      return fileResponse.getBytes();
    }
    return stringResponse.getBytes();
  }

  @Override
  public void setCharacterEncoding(String charset) {
    // Not implemented
  }

  @Override
  public void setContentLength(int len) {
    this.contentLength = len;
  }

  @Override
  public void setContentLengthLong(long len) {
    this.contentLength = len;
  }

  @Override
  public void setContentType(String type) {
    this.contentType = type;
  }

  @Override
  public void setBufferSize(int size) {
    // Not implemented
  }

  @Override
  public int getBufferSize() {
    return 0;
  }

  @Override
  public void flushBuffer() {
    // Not implemented
  }

  @Override
  public void resetBuffer() {
    // Not implemented
  }

  @Override
  public boolean isCommitted() {
    return false;
  }

  @Override
  public void reset() {
    // Not implemented
  }

  @Override
  public void setLocale(Locale loc) {
    // Not implemented
  }

  @Override
  public Locale getLocale() {
    return null;
  }

  public Map<String, Object> getHeaders() {
    return this.headers;
  }

  public long getContentLength() {
    return contentLength;
  }
}
