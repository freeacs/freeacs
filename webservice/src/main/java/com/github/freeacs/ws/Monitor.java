package com.github.freeacs.ws;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Monitor extends HttpServlet {
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    PrintWriter out = res.getWriter();
    String status = "FREEACSOK";
    out.println(status);
    out.close();
  }
}
