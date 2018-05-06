package com.owera.xaps.base.http;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.owera.common.log.Context;
import com.owera.xaps.base.BaseCache;
import com.owera.xaps.base.DownloadLogic;
import com.owera.xaps.base.Log;
import com.owera.xaps.base.db.DBAccess;
import com.owera.xaps.base.db.DBAccessStatic;
import com.owera.xaps.dbi.File;
import com.owera.xaps.dbi.FileType;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.tr069.HTTPReqResData;
import com.owera.xaps.tr069.Properties;

public class FileServlet extends HttpServlet {

  private static final long serialVersionUID = -9027563648829505599L;

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    String firmwareName = null;
    String unittypeName = null;
    OutputStream out = null;
    String authUnittypeName = null;

    try {
      // Create the main object which contains all objects concerning the entire
      // session. This object also contains the SessionData object
      if (Properties.isFileAuthUsed()) {
        HTTPReqResData reqRes = new HTTPReqResData(req, res);
        // 2. Authenticate the client (first issue challenge, then authenticate)
        if (!Authenticator.authenticate(reqRes))
          return;
        if (reqRes.getSessionData() != null && reqRes.getSessionData().getUnittype() != null) {
          authUnittypeName = reqRes.getSessionData().getUnittype().getName();
        }
      }

      XAPS xaps = DBAccess.getDBI().getXaps();
      File firmware = null;
      String pathInfo = req.getPathInfo().substring(1);
      pathInfo = pathInfo.replaceAll("--", " ");
      String[] pathInfoArr = pathInfo.split("/");
      FileType fileType = FileType.valueOf(pathInfoArr[0]); // Expect it to be a
                                                            // FileType.TYPE_SOFTWARE/TYPE_SCRIPT
      String firmwareVersion = pathInfoArr[1];
      unittypeName = pathInfoArr[2];
      if (authUnittypeName != null && !unittypeName.equals(authUnittypeName)) {
        Log.error(FileServlet.class, "Requested file in " + unittypeName + ", but was only authorized for files in " + authUnittypeName);
        res.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
    
      if (pathInfoArr.length > 3) // The optional unit-id is also sent - only
                                  // for logging purpose
        Context.put(Context.X, pathInfoArr[3], BaseCache.SESSIONDATA_CACHE_TIMEOUT);
      else
        Context.remove(Context.X);
      Unittype unittype = xaps.getUnittype(unittypeName);
      if (unittype == null) {
        Log.error(FileServlet.class, "Could not find unittype " + unittypeName + " in xAPS, hence file URL is incorrect");
        res.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      firmware = unittype.getFiles().getByVersionType(firmwareVersion, fileType);
      if (firmware == null) {
        Log.error(FileServlet.class, "Could not find " + fileType + " version " + firmwareVersion + " (in unittype " + unittype + ") in xAPS");
        res.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      firmwareName = firmware.getName();
      Log.debug(FileServlet.class, "Firmware " + firmwareName + " exists, will now retrieve binaries for unittype-name " + unittypeName);

      out = res.getOutputStream();
      byte[] firmwareImage = DBAccessStatic.readFirmwareImage(firmware);
      if (firmwareImage == null || firmwareImage.length == 0) {
        res.sendError(HttpServletResponse.SC_NOT_FOUND);
        Log.error(FileServlet.class, "No binaries found for firmware " + firmwareName + " (in unittype " + unittypeName + ")");
        return;
      }
      Log.debug(FileServlet.class, "Binaries found for firmware " + firmwareName + " (in unittype " + unittypeName + "), starts to transmit firmware image");
      res.setContentType("application/octet-stream");
      res.setContentLength(firmwareImage.length);
      out.write(firmwareImage);
      Log.debug(FileServlet.class, "Transmission of firmware " + firmwareName + " (in unittype " + unittypeName + ") ends");
    } catch (Throwable t) {
      Log.error(FileServlet.class, "Error while retrieving the firmware " + firmwareName + " (in unittype " + unittypeName + ")", t);
      res.sendError(HttpServletResponse.SC_NOT_FOUND);
    } finally {
      DownloadLogic.removeOldest();
      if (out != null) {
        out.flush();
        out.close();
      }
    }
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    doGet(req, res);
  }
}
