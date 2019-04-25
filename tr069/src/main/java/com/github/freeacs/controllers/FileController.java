package com.github.freeacs.controllers;

import com.github.freeacs.tr069.base.DBIActions;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.tr069.base.DownloadLogic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.github.freeacs.tr069.base.BaseCache.getFirmware;
import static com.github.freeacs.tr069.base.BaseCache.putFirmware;

@RestController
@Slf4j
public class FileController {

  private final DBI dbi;

  public FileController(DBI dbi) {
    this.dbi = dbi;
  }

  @GetMapping("${context-path}/file/*")
  public void doGet(@Value("${context-path}/file/") String contextPath, HttpServletRequest req, HttpServletResponse res) throws IOException {
    String firmwareName = null;
    String unittypeName = null;
    OutputStream out = null;

    try {
      ACS acs = dbi.getAcs();
      File firmware;
      String pathInfo = req.getPathInfo().substring(contextPath.length());
      pathInfo = pathInfo.replaceAll("--", " ");
      String[] pathInfoArr = pathInfo.split("/");
      FileType fileType = FileType.valueOf(pathInfoArr[0]); // Expect it to be FileType.TYPE_SOFTWARE/TYPE_SCRIPT
      String firmwareVersion = pathInfoArr[1];
      unittypeName = pathInfoArr[2];
      Unittype unittype = acs.getUnittype(unittypeName);
      if (unittype == null) {
        log.error("Could not find unittype " + unittypeName + " in xAPS, hence file URL is incorrect");
        res.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      firmware = unittype.getFiles().getByVersionType(firmwareVersion, fileType);
      if (firmware == null) {
        log.error("Could not find " + fileType + " version " + firmwareVersion + " (in unittype " + unittype + ") in xAPS");
        res.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      firmwareName = firmware.getName();
      log.debug("Firmware " + firmwareName + " exists, will now retrieve binaries for unittype-name " + unittypeName);
      out = res.getOutputStream();
      byte[] firmwareImage = readFirmwareImage(firmware);
      if (firmwareImage == null || firmwareImage.length == 0) {
        res.sendError(HttpServletResponse.SC_NOT_FOUND);
        log.error("No binaries found for firmware " + firmwareName + " (in unittype " + unittypeName + ")");
        return;
      }
      log.debug("Binaries found for firmware " + firmwareName + " (in unittype " + unittypeName + "), starts to transmit firmware image");
      res.setContentType("application/octet-stream");
      res.setContentLength(firmwareImage.length);
      out.write(firmwareImage);
      log.debug("Transmission of firmware " + firmwareName + " (in unittype " + unittypeName + ") ends");
    } catch (Throwable t) {
      log.error("Error while retrieving the firmware " + firmwareName + " (in unittype " + unittypeName + ")", t);
      res.sendError(HttpServletResponse.SC_NOT_FOUND);
    } finally {
      DownloadLogic.removeOldest();
      if (out != null) {
        out.flush();
        out.close();
      }
    }
  }

  private static byte[] readFirmwareImage(File firmwareFresh) throws SQLException {
    String action = "readFirmwareImage";
    try {
      File firmwareCache = getFirmware(firmwareFresh.getName(), firmwareFresh.getUnittype().getName());
      final File firmwareReturn;
      if (firmwareCache != null && Objects.equals(firmwareFresh.getId(), firmwareCache.getId())) {
        firmwareReturn = firmwareCache;
      } else {
        firmwareFresh.setBytes(firmwareFresh.getContent());
        putFirmware(firmwareFresh.getName(), firmwareFresh.getUnittype().getName(), firmwareFresh);
        firmwareReturn = firmwareFresh;
      }
      return firmwareReturn.getContent();
    } catch (Throwable t) {
      DBIActions.handleError(action, t);
      return null;
    }
  }
}
