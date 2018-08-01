package com.github.freeacs.tr069;

import com.github.freeacs.base.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Properties {

	public static String DIGEST_SECRET;
	public static boolean FILE_AUTH_USED;
	public static boolean DEBUG_TEST_MODE;
	public static boolean DISCOVERY_MODE;
	public static String[] DISCOVERY_BLOCK;
	public static String AUTH_METHOD;
	public static int CONCURRENT_DOWNLOAD_LIMIT;
	public static String PUBLIC_URL;

	@Autowired
	private Environment environment;

	@Value("${concurrent.download.limit:50}")
	public void setConcurrentDownloadLimit(Integer concurrentDownloadLimit) {
		CONCURRENT_DOWNLOAD_LIMIT = concurrentDownloadLimit;
	}

	@Value("${file.auth.used}")
	public void setFileAuthUsed(Boolean fileAuthUsed) {
		FILE_AUTH_USED = fileAuthUsed;
	}

	@Value("${debug.test.mode}")
	public void setDebugTestMode(Boolean testMode) {
		DEBUG_TEST_MODE = testMode;
	}

	@Value("${auth.method}")
	public void setAuthMethod(String authMethod) {
		AUTH_METHOD = authMethod;
	}

	@Value("${public.url}")
	public void setPublicUrl(String url) {
        PUBLIC_URL = url;
	}

	@Value("${digest.secret}")
	public void setDigestSecret(String digestSecret) {
		DIGEST_SECRET = digestSecret;
	}

	@Value("${discovery.mode}")
	public void setDiscoveryMode(Boolean discoveryMode) {
		DISCOVERY_MODE = discoveryMode;
	}

	@Value("${discovery.block:}")
	public void setDiscoveryBlock(String discoveryBlock) {
		DISCOVERY_BLOCK = StringUtils.isEmpty(discoveryBlock) ? new String[0] : discoveryBlock.split("\\s*,\\s*");
	}

	public boolean isParameterkeyQuirk(SessionData sessionData) {
		return isQuirk("parameterkey", sessionData.getUnittypeName(), sessionData.getVersion());
	}

	public boolean isUnitDiscovery(SessionData sessionData) {
		return isQuirk("unitdiscovery", sessionData.getUnittypeName(), sessionData.getVersion());
	}

	public boolean isTerminationQuirk(SessionData sessionData) {
		return isQuirk("termination", sessionData.getUnittypeName(), sessionData.getVersion());
	}

	public boolean isPrettyPrintQuirk(SessionData sessionData) {
		return isQuirk("prettyprint", sessionData.getUnittypeName(), sessionData.getVersion());
	}

	public boolean isIgnoreVendorConfigFile(SessionData sessionData) {
		return isQuirk("ignorevendorconfigfile", sessionData.getUnittypeName(), sessionData.getVersion());
	}

	public boolean isNextLevel0InGPN(SessionData sessionData) {
		return isQuirk("nextlevel0ingpn", sessionData.getUnittypeName(), sessionData.getVersion());
	}

	private boolean isQuirk(String quirkName, String unittypeName, String version) {
		if (unittypeName == null) {
			Log.debug(Properties.class, "The unittypename (null) could not be found. The quirk " + quirkName + " will return default false");
			return false;
		}
		for (String quirk : getQuirks(unittypeName, version)) {
			if (quirk.equals(quirkName))
				return true;
		}
		return false;
	}

	private String[] getQuirks(String unittypeName, String version) {
		String quirks = null;
		if (version != null)
			quirks = environment.getProperty("quirks." + unittypeName + "@" + version);
		if (quirks == null)
			quirks = environment.getProperty("quirks." + unittypeName);
		if (quirks == null)
			return new String[0];
		else
			return quirks.split("\\s*,\\s*");
	}
}
