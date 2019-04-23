package com.github.freeacs.base.http;

import com.github.freeacs.base.DownloadLogic;
import com.github.freeacs.base.Log;
import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.base.db.DBAccessStatic;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.http.AbstractHttpDataWrapper;
import com.github.freeacs.tr069.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class FileServlet extends AbstractHttpDataWrapper {

  private final DBAccess dbAccess;

  public FileServlet(Properties properties, DBAccess dbAccess) {
    super(properties);
    this.dbAccess = dbAccess;
  }

  @GetMapping("${context-path}/file/**")
  public void doPost(@Value("${context-path}") String contextPath, HttpServletRequest req, HttpServletResponse res) throws IOException {
    String firmwareName = null;
    String unittypeName = null;
    OutputStream out = null;

    try {
      ACS acs = dbAccess.getDbi().getAcs();
      File firmware;
      String pathInfo = req.getPathInfo().substring(contextPath.length());
      pathInfo = pathInfo.replaceAll("--", " ");
      String[] pathInfoArr = pathInfo.split("/");
      FileType fileType = FileType.valueOf(pathInfoArr[0]); // Expect it to be a
      // FileType.TYPE_SOFTWARE/TYPE_SCRIPT
      String firmwareVersion = pathInfoArr[1];
      unittypeName = pathInfoArr[2];
      Unittype unittype = acs.getUnittype(unittypeName);
      if (unittype == null) {
        Log.error(
            FileServlet.class,
            "Could not find unittype " + unittypeName + " in xAPS, hence file URL is incorrect");
        res.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      firmware = unittype.getFiles().getByVersionType(firmwareVersion, fileType);
      if (firmware == null) {
        Log.error(
            FileServlet.class,
            "Could not find "
                + fileType
                + " version "
                + firmwareVersion
                + " (in unittype "
                + unittype
                + ") in xAPS");
        res.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      firmwareName = firmware.getName();
      Log.debug(
          FileServlet.class,
          "Firmware "
              + firmwareName
              + " exists, will now retrieve binaries for unittype-name "
              + unittypeName);

      out = res.getOutputStream();
      byte[] firmwareImage = DBAccessStatic.readFirmwareImage(firmware);
      if (firmwareImage == null || firmwareImage.length == 0) {
        res.sendError(HttpServletResponse.SC_NOT_FOUND);
        Log.error(
            FileServlet.class,
            "No binaries found for firmware "
                + firmwareName
                + " (in unittype "
                + unittypeName
                + ")");
        return;
      }
      Log.debug(
          FileServlet.class,
          "Binaries found for firmware "
              + firmwareName
              + " (in unittype "
              + unittypeName
              + "), starts to transmit firmware image");
      res.setContentType("application/octet-stream");
      res.setContentLength(firmwareImage.length);
      out.write(firmwareImage);
      Log.debug(
          FileServlet.class,
          "Transmission of firmware " + firmwareName + " (in unittype " + unittypeName + ") ends");
    } catch (Throwable t) {
      Log.error(
          FileServlet.class,
          "Error while retrieving the firmware "
              + firmwareName
              + " (in unittype "
              + unittypeName
              + ")",
          t);
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
