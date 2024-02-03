package com.github.freeacs.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest.Builder;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class DigestUtil {

    /**
     * Log4j logger sent to view.
     */
    @Setter
    @Getter
    private String tokenDigest = null;

    private final String username, password, url, method;

    public DigestUtil(String username, String password, String url, String method) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.method = method;
    }

    public void parseWwwAuthenticate(Map<String, String> mapResponse) {

        if (
                mapResponse.containsKey("WWW-Authenticate")
                        && mapResponse.get("WWW-Authenticate").trim().startsWith("Digest")
        ) {

            String[] digestParts = StringUtils.split(
                    mapResponse.get("WWW-Authenticate").replaceAll("(?i)^\\s*Digest", ""),
                    ","
            );

            Map<String, String> cookieValues = Arrays.stream(digestParts)
                    .map(cookie -> {
                        String[] cookieEntry = StringUtils.split(cookie, "=");
                        return new SimpleEntry<>(
                                cookieEntry[0].trim(),
                                cookieEntry[1].trim()
                        );
                    })
                    .collect(
                            Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)
                    );

            String realm = cookieValues.get("realm").replace("\"", "");
            String qop = cookieValues.get("qop").replace("\"", "");
            String nonce = cookieValues.get("nonce").replace("\"", "");

            try {
                String nc = "00000001";
                String cnonce = "2ecb0e39da79fcb5aa6ffb1bd45cb3bb";

                URL url = new URI(this.url).toURL();
                String path = url.getFile();

                String ha1 = DigestUtils.md5Hex(
                        String.format("%s:%s:%s", username, realm, password)
                );
                String ha2 = DigestUtils.md5Hex(
                        String.format("%s:%s", this.method, path)
                );
                String response = DigestUtils.md5Hex(
                        String.format("%s:%s:%s:%s:%s:%s", ha1, nonce, nc, cnonce, qop, ha2)
                );

                this.tokenDigest = String.format(
                        "Digest username=\"%s\",realm=\"%s\",nonce=\"%s\",uri=\"%s\",cnonce=\"%s\",nc=%s,response=\"%s\",qop=\"%s\"",
                        username, realm, nonce, path, cnonce, nc, response, qop
                );

            } catch (MalformedURLException | URISyntaxException e) {

                log.error("Incorrect URL", e);
            }
        }
    }

    public void addHeaderToken(Builder httpRequest) {

        if (this.tokenDigest == null) {

            return;
        }

        httpRequest.setHeader("Authorization", this.tokenDigest);
    }

    public boolean isDigest() {
        return this.tokenDigest != null;
    }

}