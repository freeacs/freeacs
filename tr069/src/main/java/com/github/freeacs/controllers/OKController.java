package com.github.freeacs.controllers;

import com.github.freeacs.dbi.DBI;
import com.github.freeacs.tr069.base.BaseCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class OKController {
  private final DBI dbi;

  public OKController(DBI dbi) {
    this.dbi = dbi;
  }

  @GetMapping("${context-path}/ok")
  public String doGet(@RequestParam(required = false) String clearCache) {
    if (clearCache != null) {
      BaseCache.clearCache();
      log.info("Cleared base cache");
    }
    String status = "FREEACSOK";
    if (dbi != null && dbi.getDbiThrowable() != null) {
      status = "ERROR: DBI reported error:\n" +
              dbi.getDbiThrowable() + "\n" +
              Arrays.stream(dbi.getDbiThrowable().getStackTrace())
                      .map(Objects::toString)
                      .collect(Collectors.joining());
    }
    return status;
  }
}
