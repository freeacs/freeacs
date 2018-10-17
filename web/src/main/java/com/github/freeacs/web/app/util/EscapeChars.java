package com.github.freeacs.web.app.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convenience methods for escaping special characters related to HTML, XML, and regular
 * expressions.
 *
 * <p>To keep you safe by default, WEB4J goes to some effort to escape characters in your data when
 * appropriate, such that you <em>usually</em> don't need to think too much about escaping special
 * characters. Thus, you shouldn't need to <em>directly</em> use the services of this class very
 * often.
 *
 * <p><span class='highlight'>For Model Objects containing free form user input, it is highly
 * recommended that you use SafeText, not <tt>String</tt></span>. Free form user input is open to
 * malicious use, such as <a href='http://www.owasp.org/index.php/Cross_Site_Scripting'>Cross Site
 * Scripting</a> attacks. Using <tt>SafeText</tt> will protect you from such attacks, by always
 * escaping special characters automatically in its <tt>toString()</tt> method.
 */
public final class EscapeChars {
  /**
   * Escape characters for text appearing in HTML markup.
   *
   * <p>This method exists as a defence against Cross Site Scripting (XSS) hacks. The idea is to
   * neutralize control characters commonly used by scripts, such that they will not be executed by
   * the browser. This is done by replacing the control characters with their escaped equivalents.
   * See SafeText as well.
   *
   * <p>The following characters are replaced with corresponding HTML character entities :
   *
   * <table border='1' cellpadding='3' cellspacing='0'>
   * <tr><th> Character </th><th>Replacement</th></tr>
   * <tr><td> < </td><td> &lt; </td></tr>
   * <tr><td> > </td><td> &gt; </td></tr>
   * <tr><td> & </td><td> &amp; </td></tr>
   * <tr><td> " </td><td> &quot;</td></tr>
   * <tr><td> \t </td><td> &#009;</td></tr>
   * <tr><td> ! </td><td> &#033;</td></tr>
   * <tr><td> # </td><td> &#035;</td></tr>
   * <tr><td> $ </td><td> &#036;</td></tr>
   * <tr><td> % </td><td> &#037;</td></tr>
   * <tr><td> ' </td><td> &#039;</td></tr>
   * <tr><td> ( </td><td> &#040;</td></tr>
   * <tr><td> ) </td><td> &#041;</td></tr>
   * <tr><td> * </td><td> &#042;</td></tr>
   * <tr><td> + </td><td> &#043; </td></tr>
   * <tr><td> , </td><td> &#044; </td></tr>
   * <tr><td> - </td><td> &#045; </td></tr>
   * <tr><td> . </td><td> &#046; </td></tr>
   * <tr><td> / </td><td> &#047; </td></tr>
   * <tr><td> : </td><td> &#058;</td></tr>
   * <tr><td> ; </td><td> &#059;</td></tr>
   * <tr><td> = </td><td> &#061;</td></tr>
   * <tr><td> ? </td><td> &#063;</td></tr>
   * <tr><td> @ </td><td> &#064;</td></tr>
   * <tr><td> [ </td><td> &#091;</td></tr>
   * <tr><td> \ </td><td> &#092;</td></tr>
   * <tr><td> ] </td><td> &#093;</td></tr>
   * <tr><td> ^ </td><td> &#094;</td></tr>
   * <tr><td> _ </td><td> &#095;</td></tr>
   * <tr><td> ` </td><td> &#096;</td></tr>
   * <tr><td> { </td><td> &#123;</td></tr>
   * <tr><td> | </td><td> &#124;</td></tr>
   * <tr><td> } </td><td> &#125;</td></tr>
   * <tr><td> ~ </td><td> &#126;</td></tr>
   * </table>
   *
   * <p>Note that JSTL's {@code <c:out>} escapes <em>only the first five</em> of the above
   * characters.
   *
   * @param aText the a text
   * @return the string
   */
  public static String forHTML(String aText) {
    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator = new StringCharacterIterator(aText);
    char character = iterator.current();
    while (character != CharacterIterator.DONE) {
      switch (character) {
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        case '&':
          result.append("&amp;");
          break;
        case '\"':
          result.append("&quot;");
          break;
        case '\t':
          addCharEntity(9, result);
          break;
        case '!':
          addCharEntity(33, result);
          break;
        case '#':
          addCharEntity(35, result);
          break;
        case '$':
          addCharEntity(36, result);
          break;
        case '%':
          addCharEntity(37, result);
          break;
        case '\'':
          addCharEntity(39, result);
          break;
        case '(':
          addCharEntity(40, result);
          break;
        case ')':
          addCharEntity(41, result);
          break;
        case '*':
          addCharEntity(42, result);
          break;
        case '+':
          addCharEntity(43, result);
          break;
        case ',':
          addCharEntity(44, result);
          break;
        case '-':
          addCharEntity(45, result);
          break;
        case '.':
          addCharEntity(46, result);
          break;
        case '/':
          addCharEntity(47, result);
          break;
        case ':':
          addCharEntity(58, result);
          break;
        case ';':
          addCharEntity(59, result);
          break;
        case '=':
          addCharEntity(61, result);
          break;
        case '?':
          addCharEntity(63, result);
          break;
        case '@':
          addCharEntity(64, result);
          break;
        case '[':
          addCharEntity(91, result);
          break;
        case '\\':
          addCharEntity(92, result);
          break;
        case ']':
          addCharEntity(93, result);
          break;
        case '^':
          addCharEntity(94, result);
          break;
        case '_':
          addCharEntity(95, result);
          break;
        case '`':
          addCharEntity(96, result);
          break;
        case '{':
          addCharEntity(123, result);
          break;
        case '|':
          addCharEntity(124, result);
          break;
        case '}':
          addCharEntity(125, result);
          break;
        case '~':
          addCharEntity(126, result);
          break;
        default:
          // the char is not a special one
          // add it to the result as is
          result.append(character);
          break;
      }
      character = iterator.next();
    }
    return result.toString();
  }

  /**
   * Escape all ampersand characters in a URL.
   *
   * <p>Replaces all <tt>'&'</tt> characters with <tt>'&amp;'</tt>.
   *
   * <p>An ampersand character may appear in the query string of a URL. The ampersand character is
   * indeed valid in a URL. <em>However, URLs usually appear as an <tt>HREF</tt> attribute, and such
   * attributes have the additional constraint that ampersands must be escaped.</em>
   *
   * <p>The JSTL <c:url> tag does indeed perform proper URL encoding of query parameters. But it
   * does not, in general, produce text which is valid as an <tt>HREF</tt> attribute, simply because
   * it does not escape the ampersand character. This is a nuisance when multiple query parameters
   * appear in the URL, since it requires a little extra work.
   *
   * @param aURL the a url
   * @return the string
   */
  public static String forHrefAmpersand(String aURL) {
    return aURL.replace("&", "&amp;");
  }

  /**
   * Synonym for <tt>URLEncoder.encode(String, "UTF-8")</tt>.
   *
   * <p>Used to ensure that HTTP query strings are in proper form, by escaping special characters
   * such as spaces.
   *
   * <p>It is important to note that if a query string appears in an <tt>HREF</tt> attribute, then
   * there are two issues - ensuring the query string is valid HTTP (it is URL-encoded), and
   * ensuring it is valid HTML (ensuring the ampersand is escaped).
   *
   * @param aURLFragment the a url fragment
   * @return the string
   */
  public static String forURL(String aURLFragment) {
    String result = null;
    try {
      result = URLEncoder.encode(aURLFragment, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException("UTF-8 not supported", ex);
    }
    return result;
  }

  /**
   * Escape characters for text appearing as XML data, between tags.
   *
   * <p>The following characters are replaced with corresponding character entities :
   *
   * <table border='1' cellpadding='3' cellspacing='0'>
   * <tr><th> Character </th><th> Encoding </th></tr>
   * <tr><td> < </td><td> &lt; </td></tr>
   * <tr><td> > </td><td> &gt; </td></tr>
   * <tr><td> & </td><td> &amp; </td></tr>
   * <tr><td> " </td><td> &quot;</td></tr>
   * <tr><td> ' </td><td> &#039;</td></tr>
   * </table>
   *
   * <p>Note that JSTL's {@code <c:out>} escapes the exact same set of characters as this method.
   * <span class='highlight'>That is, {@code <c:out>} is good for escaping to produce valid XML, but
   * not for producing safe HTML.</span>
   *
   * @param aText the a text
   * @return the string
   */
  public static String forXML(String aText) {
    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator = new StringCharacterIterator(aText);
    char character = iterator.current();
    while (character != CharacterIterator.DONE) {
      switch (character) {
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        case '\"':
          result.append("&quot;");
          break;
        case '\'':
          result.append("&#039;");
          break;
        case '&':
          result.append("&amp;");
          break;
        default:
          // the char is not a special one
          // add it to the result as is
          result.append(character);
          break;
      }
      character = iterator.next();
    }
    return result.toString();
  }

  /**
   * Escapes characters for text appearing as data in the <a href='http://www.json.org/'>Javascript
   * Object Notation</a> (JSON) data interchange format.
   *
   * <p>The following commonly used control characters are escaped :
   *
   * <table border='1' cellpadding='3' cellspacing='0'>
   * <tr><th> Character </th><th> Escaped As </th></tr>
   * <tr><td> " </td><td> \" </td></tr>
   * <tr><td> \ </td><td> \\ </td></tr>
   * <tr><td> / </td><td> \/ </td></tr>
   * <tr><td> back space </td><td> \b </td></tr>
   * <tr><td> form feed </td><td> \f </td></tr>
   * <tr><td> line feed </td><td> \n </td></tr>
   * <tr><td> carriage return </td><td> \r </td></tr>
   * <tr><td> tab </td><td> \t </td></tr>
   * </table>
   *
   * <p>See <a href='http://www.ietf.org/rfc/rfc4627.txt'>RFC 4627</a> for more information.
   *
   * @param aText the a text
   * @return the string
   */
  public static String forJSON(String aText) {
    final StringBuilder result = new StringBuilder();
    StringCharacterIterator iterator = new StringCharacterIterator(aText);
    char character = iterator.current();
    while (character != StringCharacterIterator.DONE) {
      switch (character) {
        case '\"':
          result.append("\\\"");
          break;
        case '\\':
          result.append("\\\\");
          break;
        case '/':
          result.append("\\/");
          break;
        case '\b':
          result.append("\\b");
          break;
        case '\f':
          result.append("\\f");
          break;
        case '\n':
          result.append("\\n");
          break;
        case '\r':
          result.append("\\r");
          break;
        case '\t':
          result.append("\\t");
          break;
        default:
          // the char is not a special one
          // add it to the result as is
          result.append(character);
          break;
      }
      character = iterator.next();
    }
    return result.toString();
  }

  /**
   * Return <tt>aText</tt> with all <tt>'<'</tt> and <tt>'>'</tt> characters replaced by their
   * escaped equivalents.
   *
   * @param aText the a text
   * @return the string
   */
  public static String toDisableTags(String aText) {
    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator = new StringCharacterIterator(aText);
    char character = iterator.current();
    while (character != CharacterIterator.DONE) {
      switch (character) {
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        default:
          // the char is not a special one
          // add it to the result as is
          result.append(character);
          break;
      }
      character = iterator.next();
    }
    return result.toString();
  }

  /**
   * Replace characters having special meaning in regular expressions with their escaped
   * equivalents, preceded by a '\' character.
   *
   * <p>The escaped characters include :
   *
   * <ul>
   *   <li>.
   *   <li>\
   *   <li>?, * , and +
   *   <li>&
   *   <li>:
   *   <li>{ and }
   *   <li>[ and ]
   *   <li>( and )
   *   <li>^ and $
   * </ul>
   *
   * @param aRegexFragment the a regex fragment
   * @return the string
   */
  public static String forRegex(String aRegexFragment) {
    final StringBuilder result = new StringBuilder();

    final StringCharacterIterator iterator = new StringCharacterIterator(aRegexFragment);
    char character = iterator.current();
    while (character != CharacterIterator.DONE) {
      /* All literals need to have backslashes doubled. */
      switch (character) {
        case '.':
          result.append("\\.");
          break;
        case '\\':
          result.append("\\\\");
          break;
        case '?':
          result.append("\\?");
          break;
        case '*':
          result.append("\\*");
          break;
        case '+':
          result.append("\\+");
          break;
        case '&':
          result.append("\\&");
          break;
        case ':':
          result.append("\\:");
          break;
        case '{':
          result.append("\\{");
          break;
        case '}':
          result.append("\\}");
          break;
        case '[':
          result.append("\\[");
          break;
        case ']':
          result.append("\\]");
          break;
        case '(':
          result.append("\\(");
          break;
        case ')':
          result.append("\\)");
          break;
        case '^':
          result.append("\\^");
          break;
        case '$':
          result.append("\\$");
          break;
        default:
          // the char is not a special one
          // add it to the result as is
          result.append(character);
          break;
      }
      character = iterator.next();
    }
    return result.toString();
  }

  /**
   * Escape <tt>'$'</tt> and <tt>'\'</tt> characters in replacement strings.
   *
   * <p>Synonym for <tt>Matcher.quoteReplacement(String)</tt>.
   *
   * <p>The following methods use replacement strings which treat <tt>'$'</tt> and <tt>'\'</tt> as
   * special characters:
   *
   * <ul>
   *   <li><tt>String.replaceAll(String, String)</tt>
   *   <li><tt>String.replaceFirst(String, String)</tt>
   *   <li><tt>Matcher.appendReplacement(StringBuffer, String)</tt>
   * </ul>
   *
   * <p>If replacement text can contain arbitrary characters, then you will usually need to escape
   * that text, to ensure special characters are interpreted literally.
   *
   * @param aInput the a input
   * @return the string
   */
  public static String forReplacementString(String aInput) {
    return Matcher.quoteReplacement(aInput);
  }

  /**
   * Disable all <tt><SCRIPT></tt> tags in <tt>aText</tt>.
   *
   * <p>Insensitive to case.
   *
   * @param aText the a text
   * @return the string
   */
  public static String forScriptTagsOnly(String aText) {
    String result;
    Matcher matcher = SCRIPT.matcher(aText);
    result = matcher.replaceAll("&lt;SCRIPT>");
    matcher = SCRIPT_END.matcher(result);
    return matcher.replaceAll("&lt;/SCRIPT>");
  }

  // PRIVATE //

  /** Instantiates a new escape chars. */
  private EscapeChars() {
    // empty - prevent construction
  }

  /** The Constant SCRIPT. */
  private static final Pattern SCRIPT = Pattern.compile("<SCRIPT>", Pattern.CASE_INSENSITIVE);

  /** The Constant SCRIPT_END. */
  private static final Pattern SCRIPT_END = Pattern.compile("</SCRIPT>", Pattern.CASE_INSENSITIVE);

  /**
   * Adds the char entity.
   *
   * @param aIdx the a idx
   * @param aBuilder the a builder
   */
  private static void addCharEntity(Integer aIdx, StringBuilder aBuilder) {
    String padding = "";
    if (aIdx <= 9) {
      padding = "00";
    } else if (aIdx <= 99) {
      padding = "0";
    }
    String number = padding + aIdx;
    aBuilder.append("&#").append(number).append(";");
  }
}
