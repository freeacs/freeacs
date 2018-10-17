package com.github.freeacs.web.app.util;

/*
 * Copyright 2005 Joe Walker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.servlet.http.HttpServletRequest;

/**
 * Various functions to do with working out what is at the other end of the wire, and what it can
 * do.
 *
 * <h2>Version number documentation</h2>
 *
 * <h3>Safari</h3>
 *
 * <p>Quick summary:
 *
 * <ul>
 *   <li>Jaguar = 10.2.x = Safari 1.0.x = WebKit/85
 *   <li>Panther = 10.3.0+ = Safari 1.1.x = WebKit/100
 *   <li>Panther = 10.3.4+ = Safari 1.2.x = WebKit/125
 *   <li>Panther = 10.3.9+ = Safari 1.3.x = WebKit/312
 *   <li>Tiger = 10.4.x = Safari 2.0.x = WebKit/412-419
 *   <li>Tiger = 10.4.11 = Safari 3.0.x = WebKit/523
 *   <li>Leopard = 10.5.x = Safari 3.0.x = WebKit/523
 *   <li>Windows = Safari 3.0.x = WebKit/523
 *   <li>Leopard = 10.5.x = Safari 3.1.x = WebKit/525-526
 * </ul>
 *
 * <p>For full information see the Safari and WebKit Version Information: <a
 * href="http://developer.apple.com/internet/safari/uamatrix.html">at Apple Developer Connection</a>
 * and for browsers in general, see this fairly complete <a
 * href="http://www.useragentstring.com/pages/useragentstring.php">list of user agent strings</a>.
 *
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public class BrowserDetect {
  /**
   * Less than.
   *
   * @param request the request
   * @param requiredUserAgent the required user agent
   * @param requiredVersion the required version
   * @return true, if successful
   */
  public static boolean lessThan(
      HttpServletRequest request, UserAgent requiredUserAgent, int requiredVersion) {
    int realVersion = getRealVersion(request, requiredUserAgent);
    return realVersion != -1 && realVersion <= requiredVersion; // Not IE or not applicable
  }

  /**
   * Gets the real version.
   *
   * @param request the request
   * @param requiredUserAgent the required user agent
   * @return the real version
   */
  private static int getRealVersion(HttpServletRequest request, UserAgent requiredUserAgent) {
    String userAgent = request.getHeader("user-agent");
    int realVersion;

    switch (requiredUserAgent) {
      case IE:
        realVersion = getMajorVersionAssumingIE(userAgent);
        break;

      case Gecko:
        realVersion = getMajorVersionAssumingGecko(userAgent);
        break;

      case Opera:
        realVersion = getMajorVersionAssumingOpera(userAgent);
        break;

      case AppleWebKit:
        realVersion = getMajorVersionAssumingAppleWebKit(userAgent);
        break;

      default:
        throw new UnsupportedOperationException(
            "Detection of " + requiredUserAgent + " is not supported yet.");
    }

    return realVersion;
  }

  /**
   * @param userAgent the user agent
   * @return the major version assuming apple web kit {@link UserAgent#AppleWebKit}
   */
  private static int getMajorVersionAssumingAppleWebKit(String userAgent) {
    int webKitPos = userAgent.indexOf("AppleWebKit");
    if (webKitPos == -1) {
      return -1;
    }

    return parseNumberAtStart(userAgent.substring(webKitPos + 12));
  }

  /**
   * @param userAgent the user agent
   * @return the major version assuming opera {@link UserAgent#Opera}
   */
  private static int getMajorVersionAssumingOpera(String userAgent) {
    int operaPos = userAgent.indexOf("Opera");
    if (operaPos == -1) {
      return -1;
    }

    return parseNumberAtStart(userAgent.substring(operaPos + 6));
  }

  /**
   * @param userAgent the user agent
   * @return the major version assuming gecko {@link UserAgent#Gecko}
   */
  private static int getMajorVersionAssumingGecko(String userAgent) {
    int geckoPos = userAgent.indexOf(" Gecko/20");
    if (geckoPos == -1 || userAgent.contains("WebKit/")) {
      return -1;
    }

    return parseNumberAtStart(userAgent.substring(geckoPos + 7));
  }

  /**
   * @param userAgent the user agent
   * @return the major version assuming ie {@link UserAgent#IE}
   */
  private static int getMajorVersionAssumingIE(String userAgent) {
    int msiePos = userAgent.indexOf("MSIE ");
    if (msiePos == -1 || userAgent.contains("Opera")) {
      return -1;
    }

    return parseNumberAtStart(userAgent.substring(msiePos + 5));
  }

  /**
   * We've found the start of a sequence of numbers, what is it as an int?.
   *
   * @param numberString the number string
   * @return the int
   */
  private static int parseNumberAtStart(String numberString) {
    if (numberString == null || numberString.isEmpty()) {
      return -1;
    }

    int endOfNumbers = 0;
    while (Character.isDigit(numberString.charAt(endOfNumbers))) {
      endOfNumbers++;
    }

    try {
      return Integer.parseInt(numberString.substring(0, endOfNumbers));
    } catch (NumberFormatException ex) {
      return -1;
    }
  }
}
