package com.github.freeacs.shell.output;

import java.io.IOException;
import java.io.Reader;

/** To be used in InputHandler (reads Listing like it reads a file). */
public class ListingReader extends Reader {
  private StringBuffer listingBuffer;
  private int pos;

  public ListingReader(Listing listing) {
    listingBuffer = new StringBuffer();
    for (Line line : listing.getLines()) {
      for (String value : line.getValues()) {
        listingBuffer.append(value).append(" ");
      }
      listingBuffer.append("\n");
    }
  }

  @Override
  public void close() throws IOException {}

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    if (pos >= listingBuffer.length()) {
      return -1;
    } else if (pos + len > listingBuffer.length()) {
      listingBuffer.getChars(pos, listingBuffer.length(), cbuf, off);
      int length = listingBuffer.length() - pos;
      pos = listingBuffer.length();
      return length;
    } else {
      listingBuffer.getChars(pos, pos + len, cbuf, off);
      pos += len;
      return len;
    }
  }
}
