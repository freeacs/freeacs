package com.github.freeacs.controllers;

import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.dbi.DBI;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@RestController
public class OKController {
  private final DBAccess dbAccess;

  public OKController(DBAccess dbAccess) {
    this.dbAccess = dbAccess;
  }

  @GetMapping("${context-path}/ok")
  public void doGet(HttpServletResponse res) throws IOException {
    PrintWriter out = res.getWriter();
    StringBuilder status = new StringBuilder("FREEACSOK");
    try {
      DBI dbi = dbAccess.getDbi();
      if (dbi != null && dbi.getDbiThrowable() != null) {
        status =
            new StringBuilder("ERROR: DBI reported error:\n")
                .append(dbi.getDbiThrowable())
                .append("\n");
        for (StackTraceElement ste : dbi.getDbiThrowable().getStackTrace()) {
          status.append(ste);
        }
      }
    } catch (Throwable ignored) {}
    out.print(status);
    out.close();
  }
}
