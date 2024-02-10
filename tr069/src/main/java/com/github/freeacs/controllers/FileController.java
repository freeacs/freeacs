package com.github.freeacs.controllers;

import com.github.freeacs.cache.AcsCache;
import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.tr069.base.DownloadLogic;
import com.github.freeacs.tr069.methods.decision.GetParameterValues.DownloadLogicTR069;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;

@RestController
@Slf4j
public class FileController {
  public static final String CTX_PATH = "/file";

  private final AcsCache acsCache;

  public FileController(AcsCache acsCache) {
      this.acsCache = acsCache;
  }

  @GetMapping("${context-path}" + CTX_PATH + "/{fileType}/{firmwareVersion}/{unitTypeName}")
  public void doGet(@PathVariable("fileType") FileType fileType,
                    @PathVariable("firmwareVersion") String firmwareVersion,
                    @PathVariable("unitTypeName") String unitTypeName,
                    HttpServletResponse res) throws IOException {
    var version = firmwareVersion.replaceAll(DownloadLogicTR069.SPACE_SEPARATOR, " ");
    unitTypeName = unitTypeName.replaceAll(DownloadLogicTR069.SPACE_SEPARATOR, " ");
    String firmwareName = null;
    OutputStream out = null;

    try {
      Unittype unittype = acsCache.getUnitType(unitTypeName);
      if (unittype == null) {
        log.error("Could not find unittype " + unitTypeName + " in xAPS, hence file URL is incorrect");
        res.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      File firmware = acsCache.getFile(fileType, unittype, version);
      if (firmware == null) {
        log.error("Could not find " + fileType + " version " + firmwareVersion + " (in unittype " + unittype + ") in xAPS");
        res.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      firmwareName = firmware.getName();
      log.debug("Firmware " + firmwareName + " exists, will now retrieve binaries for unittype-name " + unitTypeName);
      out = res.getOutputStream();
      byte[] firmwareImage = acsCache.getFileContents(firmware.getId());
      if (firmwareImage == null || firmwareImage.length == 0) {
        res.sendError(HttpServletResponse.SC_NOT_FOUND);
        log.error("No binaries found for firmware " + firmwareName + " (in unittype " + unitTypeName + ")");
        return;
      }
      log.debug("Binaries found for firmware " + firmwareName + " (in unittype " + unitTypeName + "), starts to transmit firmware image");
      res.setContentType("application/octet-stream");
      res.setContentLength(firmwareImage.length);
      out.write(firmwareImage);
      log.debug("Transmission of firmware " + firmwareName + " (in unittype " + unitTypeName + ") ends");
    } catch (Throwable t) {
      log.error("Error while retrieving the firmware " + firmwareName + " (in unittype " + unitTypeName + ")", t);
      res.sendError(HttpServletResponse.SC_NOT_FOUND);
    } finally {
      DownloadLogic.removeOldest();
      if (out != null) {
        out.flush();
        out.close();
      }
    }
  }
}
