package com.github.freeacs.common.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class FileResponse extends ServletOutputStream {
  private ByteArrayOutputStream outputStream;

  public FileResponse() {
    outputStream = new ByteArrayOutputStream();
  }

  @Override
  public boolean isReady() {
    return false;
  }

  @Override
  public void setWriteListener(WriteListener writeListener) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void write(int i) {
    outputStream.write(i);
  }

  public void flush() throws IOException {
    outputStream.flush();
  }

  public void close() throws IOException {
    outputStream.close();
  }

  public byte[] getBytes() {
    return outputStream.toByteArray();
  }
}
