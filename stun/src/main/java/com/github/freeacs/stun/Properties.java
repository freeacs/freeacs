package com.github.freeacs.stun;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Properties {

	public static boolean EXPECT_PORT_FORWARDING;
	public static boolean RUN_WITH_STUN;
	public static String SECONDARY_IP;
	public static String PRIMARY_IP;
	public static Integer SECONDARY_PORT;
	public static Integer PRIMARY_PORT;
	public static Integer KICK_INTERVAL;
	public static boolean CHECK_PUBLIC_IP;
	public static Integer KICK_RESCAN;

	@Value("${kick.rescan:60}")
	public void setKickRescan(Integer kickRescan) {
		KICK_RESCAN = kickRescan;
	}

	@Value("${kick.check-public-ip:false}")
	public void setCheckPublicIp(Boolean checkPublicIp) {
		CHECK_PUBLIC_IP = checkPublicIp;
	}

	@Value("${kick.interval:1000}")
	public void setKickInterval(Integer kickInterval) {
		KICK_INTERVAL = kickInterval;
	}

	@Value("${primary.port:3478}")
	public void setPrimaryPort(Integer port) {
		PRIMARY_PORT = port;
	}

	@Value("${secondary.port:3479}")
	public void setSecondaryPort(Integer port) {
		SECONDARY_PORT = port;
	}

	@Value("${primary.ip:#{null}}")
	public void setPrimaryIp(String ip) {
		PRIMARY_IP = ip;
	}

	@Value("${secondary.ip:#{null}}")
	public void setSecondaryIp(String ip) {
		SECONDARY_IP = ip;
	}

	@Value("${test.runwithstun:true}")
	public void setRunWithStun(Boolean runWithStun) {
		RUN_WITH_STUN = runWithStun;
	}

	@Value("${kick.expect-port-forwarding:false}")
	public void setExpectPortForwarding(Boolean expectPortForwarding) {
		EXPECT_PORT_FORWARDING = expectPortForwarding;
	}

}
