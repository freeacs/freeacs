package com.github.freeacs.tr069.http;

import com.github.freeacs.tr069.xml.Fault;
import com.github.freeacs.tr069.xml.Parser;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;

@Getter
@Setter
public class HTTPRequestData {
  private String method;
  private Parser parser;
  private Fault fault;
  private String contextPath;
}
