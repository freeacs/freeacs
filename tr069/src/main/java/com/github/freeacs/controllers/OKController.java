package com.github.freeacs.controllers;

import com.github.freeacs.dbi.DBI;
import com.github.freeacs.tr069.base.BaseCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class OKController {
  public static final String CTX_PATH = "/ok";

  private final DBI dbi;

  public OKController(DBI dbi) {
    this.dbi = dbi;
  }

  @GetMapping("${context-path}" + CTX_PATH)
  public String doGet(@RequestParam(required = false) String clearCache) {
    if (clearCache != null) {
      BaseCache.clearCache();
      log.info("Cleared base cache");
    }
    String status = "FREEACSOK";
    if (dbi != null && !dbi.isRunning()) {
      status = "ERROR: DBI is not running";
    }
    return status;
  }
}
