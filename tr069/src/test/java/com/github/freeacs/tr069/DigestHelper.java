package com.github.freeacs.tr069;

import com.github.freeacs.base.http.DigestAuthenticator;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class DigestHelper {

    public static String getDigestAuthentication(String challenge, String uri, String nc, String cnonce, String qop, String user, String pass) {
        Map<String, String> props = getDigestMap(challenge);
        return DigestAuthenticator.passwordMd5(user, pass, "POST", uri, props.get("nonce"), nc, cnonce, qop);
    }

    public static Map<String, String> getDigestMap(String challenge) {
        return Arrays.stream(challenge.substring(7).split(","))
                .map(line -> line.split("="))
                .collect(Collectors.toMap(l -> l[0], l -> l[1], (o, n) -> o));
    }
}
