package com.github.freeacs.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

public class CsrfSecurityRequestMatcher implements RequestMatcher {
    private static Logger logger = LoggerFactory.getLogger(CsrfSecurityRequestMatcher.class);

    private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
    private RegexRequestMatcher unprotectedMatcher = new RegexRequestMatcher("^(?!/(login|logout)*$).*", null);

    @Override
    public boolean matches(HttpServletRequest request) {
        logger.info(request.getRequestURI());
        if(allowedMethods.matcher(request.getMethod()).matches()){
            return false;
        }
        boolean matches = !unprotectedMatcher.matches(request);
        logger.info("Matches: " + matches);
        return matches;
    }
}