package com.github.freeacs.tr069.methods;

import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.util.ProvisioningMessage;
import com.github.freeacs.dbi.util.ProvisioningMessage.ProvOutput;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.xml.Body;
import com.github.freeacs.tr069.xml.Header;
import com.github.freeacs.tr069.xml.Response;
import com.github.freeacs.tr069.xml.TR069TransactionID;

/**
 * The class is responsible for creating a suitable response to the CPE. This response could be a
 * TR-069 request or a TR-069 response.
 *
 * @author morten
 */
public interface HTTPResponseCreator {

  static Response buildRE(HTTPRequestResponseData reqRes) {
    if (reqRes.getTR069TransactionID() == null) {
      reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
    }
    TR069TransactionID tr069ID = reqRes.getTR069TransactionID();
    Header header = new Header(tr069ID, null, null);
    Body body = new REreq();
    return new Response(header, body, reqRes.getSessionData().getCwmpVersionNumber());
  }

  static Response buildFR(HTTPRequestResponseData reqRes) {
    if (reqRes.getTR069TransactionID() == null) {
      reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
    }
    TR069TransactionID tr069ID = reqRes.getTR069TransactionID();
    Header header = new Header(tr069ID, null, null);
    Body body = new FRreq();
    return new Response(header, body, reqRes.getSessionData().getCwmpVersionNumber());
  }

  static Response buildDO(HTTPRequestResponseData reqRes, boolean fileAuthUsed) {
    if (reqRes.getTR069TransactionID() == null) {
      reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
    }
    Header header = new Header(reqRes.getTR069TransactionID(), null, null);
    SessionData sessionData = reqRes.getSessionData();
    SessionData.Download download = sessionData.getDownload();
    ProvisioningMessage pm = sessionData.getProvisioningMessage();
    String downloadType = null;
    String tn = download.getFile().getTargetName();
    String commandKey = download.getFile().getVersion();
    if (download.getFile().getType() == FileType.SOFTWARE) {
      downloadType = DOreq.FILE_TYPE_FIRMWARE;
      pm.setProvOutput(ProvOutput.SOFTWARE);
    }
    if (download.getFile().getType() == FileType.TR069_SCRIPT) {
      downloadType = DOreq.FILE_TYPE_CONFIG;
      pm.setProvOutput(ProvOutput.SCRIPT);
    }
    String version = download.getFile().getVersion();
    pm.setFileVersion(version);
    String username = sessionData.getUnitId();
    String password = sessionData.getAcsParameters().getValue(SystemParameters.SECRET);
    Body body =
        new DOreq(
            download.getUrl(),
            downloadType,
            tn,
            download.getFile().getLength(),
            commandKey,
            username,
            password,
            fileAuthUsed);
    return new Response(header, body, reqRes.getSessionData().getCwmpVersionNumber());
  }

}
