package com.github.freeacs.tr069.methods.response;

import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.util.ProvisioningMessage;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.xml.Body;
import com.github.freeacs.tr069.xml.Header;
import com.github.freeacs.tr069.xml.Response;
import com.github.freeacs.tr069.xml.TR069TransactionID;

public class DownloadResponseCreateStrategy implements ResponseCreateStrategy {
    private final String START = "\t\t<cwmp:Download xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n";
    private final String COMMAND_KEY_START = "\t\t\t<CommandKey>";
    private final String COMMAND_KEY_END = "</CommandKey>\n";
    private final String FILE_TYPE_START = "\t\t\t<FileType>";
    private final String FILE_TYPE_END = "</FileType>\n";
    private final String URL_START = "\t\t\t<URL>";
    private final String URL_END = "</URL>\n";
    private final String USERNAME_START = "\t\t\t<Username>";
    private final String USERNAME_END = "</Username>\n";
    private final String PASSWORD_START = "\t\t\t<Password>";
    private final String PASSWORD_END = "</Password>\n";
    private final String DELAY_SECONDS_START = "\t\t\t<DelaySeconds>";
    private final String DELAY_SECONDS = "0";
    private final String DELAY_SECONDS_END = "</DelaySeconds>\n";
    private final String SUCCESS_URL = "\t\t\t<SuccessURL></SuccessURL>\n";
    private final String FAILURE_URL = "\t\t\t<FailureURL></FailureURL>\n";
    private final String END = "\t\t</cwmp:Download>\n";

    private final Properties properties;

    public DownloadResponseCreateStrategy(Properties properties) {
        this.properties = properties;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Response getResponse(HTTPRequestResponseData reqRes) {
        if (reqRes.getTR069TransactionID() == null) {
            reqRes.setTR069TransactionID(new TR069TransactionID("FREEACS-" + System.currentTimeMillis()));
        }
        Header header = new Header(reqRes.getTR069TransactionID(), null, null);
        SessionData sessionData = reqRes.getSessionData();
        SessionData.Download download = sessionData.getDownload();
        ProvisioningMessage pm = sessionData.getProvisioningMessage();
        final String downloadType;
        String tn = download.getFile().getTargetName();
        String commandKey = download.getFile().getVersion();
        if (download.getFile().getType() == FileType.SOFTWARE) {
            downloadType = "1 Firmware Upgrade Image";
            pm.setProvOutput(ProvisioningMessage.ProvOutput.SOFTWARE);
        } else if (download.getFile().getType() == FileType.TR069_SCRIPT) {
            downloadType = "3 Vendor Configuration File";
            pm.setProvOutput(ProvisioningMessage.ProvOutput.SCRIPT);
        } else {
            downloadType = null;
        }
        String version = download.getFile().getVersion();
        pm.setFileVersion(version);
        String username = sessionData.getUnitId();
        String password = sessionData.getAcsParameters().getValue(SystemParameters.SECRET);
        Body body = new Body() {
            private boolean fileAuthUsed = properties.isFileAuthUsed();
            private String url = download.getUrl();
            private String type = downloadType;
            private int filesize = download.getFile().getLength();
            private String targetFilename = tn;

            @Override
            public String toXmlImpl() {
                StringBuilder sb = new StringBuilder(3);
                sb.append(START);
                sb.append(COMMAND_KEY_START);
                if (commandKey != null) {
                    sb.append(commandKey);
                } else {
                    sb.append("Download_To_CPE");
                }
                sb.append(COMMAND_KEY_END);
                sb.append(FILE_TYPE_START);
                sb.append(type);
                sb.append(FILE_TYPE_END);
                sb.append(URL_START);
                sb.append(url);
                sb.append(URL_END);

                sb.append(USERNAME_START);
                if (fileAuthUsed) {
                    sb.append(username);
                }
                sb.append(USERNAME_END);
                sb.append(PASSWORD_START);
                if (fileAuthUsed) {
                    sb.append(password);
                }
                sb.append(PASSWORD_END);
                sb.append("\t\t\t<FileSize>").append(filesize).append("</FileSize>\n");
                if (targetFilename != null) {
                    sb.append("\t\t\t<TargetFileName>").append(targetFilename).append("</TargetFileName>\n");
                } else {
                    sb.append("\t\t\t<TargetFileName></TargetFileName>\n");
                }
                sb.append(DELAY_SECONDS_START);
                sb.append(DELAY_SECONDS);
                sb.append(DELAY_SECONDS_END);
                sb.append(SUCCESS_URL);
                sb.append(FAILURE_URL);
                sb.append(END);
                return sb.toString();
            }
        };
        return new Response(header, body, reqRes.getSessionData().getCwmpVersionNumber());
    }
}
