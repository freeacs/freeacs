package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.UnittypeParameters;
import com.github.freeacs.dbi.tr069.TestCaseParameter;
import com.github.freeacs.dbi.tr069.TestCaseParameter.TestCaseParameterType;
import com.github.freeacs.dbi.util.ProvisioningMessage;
import com.github.freeacs.dbi.util.ProvisioningMessage.ProvOutput;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.tr069.*;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.exception.TR069ExceptionShortMessage;
import com.github.freeacs.tr069.test.system2.TestUnit;
import com.github.freeacs.tr069.test.system2.TestUnitCache;
import com.github.freeacs.tr069.test.system2.Util;
import com.github.freeacs.tr069.xml.*;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

/**
 * The class is responsible for creating a suitable response to the CPE. This
 * response could be a TR-069 request or a TR-069 response.
 *
 * @author morten
 *
 */
public interface HTTPResponseCreator {

  static Response buildEM(HTTPReqResData reqRes) {
    return new EmptyResponse();
  }

  static Response buildRE(HTTPReqResData reqRes) {
    if (reqRes.getTR069TransactionID() == null)
      reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
    TR069TransactionID tr069ID = reqRes.getTR069TransactionID();
    Header header = new Header(tr069ID, null, null);
    Body body = new REreq();
    return new Response(header, body);
  }

  static Response buildFR(HTTPReqResData reqRes) {
    if (reqRes.getTR069TransactionID() == null)
      reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
    TR069TransactionID tr069ID = reqRes.getTR069TransactionID();
    Header header = new Header(tr069ID, null, null);
    Body body = new FRreq();
    return new Response(header, body);
  }

  static Response buildTC(HTTPReqResData reqRes) {
    TR069TransactionID tr069ID = reqRes.getTR069TransactionID();
    Header header = new Header(tr069ID, null, null);
    Body body = new TCres();
    return new Response(header, body);
  }

  static Response buildIN(HTTPReqResData reqRes) {
    TR069TransactionID tr069ID = reqRes.getTR069TransactionID();
    Header header = new Header(tr069ID, null, null);
    Body body = new INres();
    return new Response(header, body);
  }

   static Response buildGRMReq(HTTPReqResData reqRes) {
    TR069TransactionID tr069ID = reqRes.getTR069TransactionID();
    Header header = new Header(tr069ID, null, null);
    Body body = new GRMreq();
    return new Response(header, body);
  }

   static Response buildGRMRes(HTTPReqResData reqRes) {
    TR069TransactionID tr069ID = reqRes.getTR069TransactionID();
    Header header = new Header(tr069ID, null, null);
    Body body = new GRMres();
    return new Response(header, body);
  }

   static Response buildCU(HTTPReqResData reqRes) {
    TR069TransactionID tr069ID = reqRes.getTR069TransactionID();
    if (reqRes.getTR069TransactionID() == null)
      reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
    Header header = new Header(reqRes.getTR069TransactionID(), null, null);
    SessionData sessionData = reqRes.getSessionData();
    String keyRoot = sessionData.getKeyRoot();
    String unitId = sessionData.getUnitId();
    Body body = new CUreq(keyRoot, unitId);
    return new Response(header, body);
  }

   static Response buildGPN(HTTPReqResData reqRes, Properties properties) {
    if (reqRes.getTR069TransactionID() == null)
      reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
    Header header = new Header(reqRes.getTR069TransactionID(), null, null);
    String keyRoot = reqRes.getSessionData().getKeyRoot();
    Body body = new GPNreq(keyRoot, properties.isNextLevel0InGPN(reqRes.getSessionData()));
    return new Response(header, body);
  }

   static Response buildGPA(HTTPReqResData reqRes) {
    if (reqRes.getTR069TransactionID() == null)
      reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
    Header header = new Header(reqRes.getTR069TransactionID(), null, null);
    String keyRoot = reqRes.getSessionData().getKeyRoot();
    List<ParameterAttributeStruct> parameterAttributeList = new ArrayList<ParameterAttributeStruct>();
    if (Util.testEnabled(reqRes, false)) {
      // TODO:TF - build GPA - completed
      TestUnit tu = TestUnitCache.get(reqRes.getSessionData().getUnitId());
      if (tu != null) {
        List<TestCaseParameter> params = tu.getCurrentCase().getParams();
        for (TestCaseParameter param : params) {
          if (param.getType() == TestCaseParameterType.GET) {
            parameterAttributeList.add(new ParameterAttributeStruct(param.getUnittypeParameter().getName(), param.getNotification()));
          }
        }
      }
    }
    Body body = new GPAreq(parameterAttributeList);
    return new Response(header, body);
  }

  static boolean isOldPingcomDevice(String unitId) {
    if (unitId.contains("NPA201E"))
      return true;
    if (unitId.contains("RGW208EN"))
      return true;
    if (unitId.contains("NPA101E"))
      return true;
    return false;
  }

  static void addCPEParameters(SessionData sessionData, Properties properties) {
    Map<String, ParameterValueStruct> paramValueMap = sessionData.getFromDB();
    CPEParameters cpeParams = sessionData.getCpeParameters();
    UnittypeParameters utps = sessionData.getUnittype().getUnittypeParameters();

    // If device is not old Ping Communication device (NPA201E or RGW208EN) and
    // vendor config file is not explicitely turned off,
    // then we may ask for VendorConfigFile object.
    boolean useVendorConfigFile = !isOldPingcomDevice(sessionData.getUnitId()) && !properties.isIgnoreVendorConfigFile(sessionData);
    if (useVendorConfigFile)
      Log.debug(HTTPResponseCreator.class, "VendorConfigFile object will be requested (default behavior)");
    else
      Log.debug(HTTPResponseCreator.class, "VendorConfigFile object will not be requested. (quirk behavior: old Pingcom device or quirk enabled)");

    int counter = 0;
    for (String key : cpeParams.getCpeParams().keySet()) {
      if (key.endsWith(".") && useVendorConfigFile) {
        paramValueMap.put(key, new ParameterValueStruct(key, "ExtraCPEParam"));
        counter++;
      } else if (paramValueMap.get(key) == null && utps.getByName(key) != null) {
        paramValueMap.put(key, new ParameterValueStruct(key, "ExtraCPEParam"));
        counter++;
      }
    }
    Log.debug(HTTPResponseCreator.class, counter + " cpe-param (not found in database, but of special interest to ACS) added to the GPV-request");
  }

  /**
   * Special treatment for PeriodicInformInterval, we want to get that parameter
   * from the CPE, regardless of what parameteres we have in the database.
   * That's because we have 2 different ways to set it in the database, the
   * standard and the FREEACS-way.
   *
   *
   */
   static Response buildGPV(HTTPReqResData reqRes, Properties properties) {
    if (reqRes.getTR069TransactionID() == null)
      reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
    Header header = new Header(reqRes.getTR069TransactionID(), null, null);
    SessionData sessionData = reqRes.getSessionData();
    ProvisioningMode mode = sessionData.getUnit().getProvisioningMode();
    List<ParameterValueStruct> parameterValueList = new ArrayList<ParameterValueStruct>();
    if (Util.testEnabled(reqRes, false)) {
      // TODO:TF - build GPV - completed
      TestUnit tu = TestUnitCache.get(sessionData.getUnitId());
      if (tu != null) {
        List<TestCaseParameter> params = tu.getCurrentCase().getParams();
        for (TestCaseParameter param : params) {
          if (param.getType() == TestCaseParameterType.GET) {
            ParameterValueStruct pvs = new ParameterValueStruct(param.getUnittypeParameter().getName(), "");
            parameterValueList.add(pvs);
          }
        }
      }
    } else if (mode == ProvisioningMode.READALL) {
      Log.debug(HTTPResponseCreator.class, "Asks for all params (" + sessionData.getKeyRoot() + "), since in " + ProvisioningMode.READALL.toString() + " mode");
      ParameterValueStruct pvs = new ParameterValueStruct(sessionData.getKeyRoot(), "");
      parameterValueList.add(pvs);
    } else { // mode == ProvisioningMode.PERIODIC
      // List<RequestResponseData> reqResList = sessionData.getReqResList();
      String previousMethod = sessionData.getPreviousResponseMethod();
      if (properties.isUnitDiscovery(sessionData) || previousMethod.equals(TR069Method.GET_PARAMETER_VALUES)) {
        Log.debug(HTTPResponseCreator.class, "Asks for all params (" + sessionData.getKeyRoot() + "), either because unitdiscovery-quirk or prev. GPV failed");
        ParameterValueStruct pvs = new ParameterValueStruct(sessionData.getKeyRoot(), "");
        parameterValueList.add(pvs);
      } else {
        Map<String, ParameterValueStruct> paramValueMap = sessionData.getFromDB();
        UnittypeParameters utps = sessionData.getUnittype().getUnittypeParameters();
        addCPEParameters(sessionData, properties);
        for (Entry<String, ParameterValueStruct> entry : paramValueMap.entrySet()) {
          parameterValueList.add(entry.getValue());
        }
        Log.debug(HTTPResponseCreator.class, "Asks for " + parameterValueList.size() + " parameters in GPV-req");
        Collections.sort(parameterValueList, new ParameterValueStructComparator());
      }
    }
    sessionData.setRequestedCPE(parameterValueList);
    Body body = new GPVreq(parameterValueList);
    return new Response(header, body);
  }

   static Response buildSPV(HTTPReqResData reqRes, Properties properties) throws NoSuchAlgorithmException, SQLException {
    if (reqRes.getTR069TransactionID() == null)
      reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
    Header header = new Header(reqRes.getTR069TransactionID(), null, null);
    Body body = null;
    ParameterList paramList = new ParameterList();
    if (Util.testEnabled(reqRes, false)) {
      // TODO:TF - build SPV - completed
      TestUnit tu = TestUnitCache.get(reqRes.getSessionData().getUnitId());
      if (tu != null) {
        List<TestCaseParameter> params = tu.getCurrentCase().getParams();
        for (TestCaseParameter param : params) {
          if (param.getType() == TestCaseParameterType.SET) {
            ParameterValueStruct pvs = new ParameterValueStruct(param.getUnittypeParameter().getName(), param.getValue());
            if (param.getDataModelParameter() == null)
              Log.error(HTTPResponseCreator.class, "Could not find datamodel parameter for " + param.getUnittypeParameter().getName());
            else
              pvs.setType(param.getDataModelParameter().getDatatype().getXsdType());
            paramList.addParameterValueStruct(pvs);
          }
        }
        reqRes.getSessionData().setToCPE(paramList);
      }
    }
    paramList = reqRes.getSessionData().getToCPE();
    ParameterKey pk = new ParameterKey();
    if (!properties.isParameterkeyQuirk(reqRes.getSessionData()))
      pk.setServerKey(reqRes);
    body = new SPVreq(paramList.getParameterValueList(), pk.getServerKey());
    Log.notice(HTTPResponseCreator.class, "Sent to CPE: " + paramList.getParameterValueList().size() + " parameters.");
    reqRes.getSessionData().getProvisioningMessage().setParamsWritten(paramList.getParameterValueList().size());
    return new Response(header, body);
  }

   static Response buildDO(HTTPReqResData reqRes) {
    if (reqRes.getTR069TransactionID() == null)
      reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
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
    Body body = new DOreq(download.getUrl(), downloadType, tn, download.getFile().getLength(), commandKey, username, password);
    return new Response(header, body);
  }

  static void createResponse(HTTPReqResData reqRes, Map<String, HTTPResponseAction> responseMap) throws TR069Exception {
    try {
      String methodName = reqRes.getResponse().getMethod();
      final Response response;
      HTTPResponseAction resAction = responseMap.get(methodName);
      if (resAction != null)
        response = resAction.getCreateResponseMethod().apply(reqRes);
      else {
        response = new EmptyResponse();
        Log.error(HTTPResponseCreator.class, "The methodName " + methodName + " has no corresponding ResponseAction-method");
      }
      String responseStr = response.toXml();
      String unitId = reqRes.getSessionData().getUnitId();
      Log.conversation(reqRes.getSessionData(), "=============== FROM ACS TO ( " + Optional.ofNullable(unitId).orElseGet(() -> "Unknown") + " ) ============\n" + responseStr + "\n");
      reqRes.getResponse().setXml(responseStr);
    } catch (Throwable t) {
      throw new TR069Exception("Not possible to create HTTP-response (to the TR-069 client)", TR069ExceptionShortMessage.MISC, t);
    }
  }

   static Response buildSPA(HTTPReqResData reqRes) {
    if (reqRes.getTR069TransactionID() == null)
      reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
    Header header = new Header(reqRes.getTR069TransactionID(), null, null);
    Body body = null;
    List<ParameterAttributeStruct> attributes = new ArrayList<ParameterAttributeStruct>();
    if (Util.testEnabled(reqRes, false)) {
      // TODO:TF - build SPA - completed
      TestUnit tu = TestUnitCache.get(reqRes.getSessionData().getUnitId());
      if (tu != null) {
        List<TestCaseParameter> params = tu.getCurrentCase().getParams();
        for (TestCaseParameter param : params) {
          if (param.getType() == TestCaseParameterType.SET) {
            attributes.add(new ParameterAttributeStruct(param.getUnittypeParameter().getName(), param.getNotification()));
          }
        }
        reqRes.getSessionData().setAttributesToCPE(attributes);
      }
    }
    attributes = reqRes.getSessionData().getAttributesToCPE();
    body = new SPAreq(attributes);
    Log.notice(HTTPResponseCreator.class, "Sent to CPE: " + attributes.size() + " attributes.");
    for (ParameterAttributeStruct pvs : attributes)
      Log.notice(HTTPResponseCreator.class, "\t" + pvs.getName() + " : " + pvs.getNotifcation());
    return new Response(header, body);
  }
}
