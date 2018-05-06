package com.owera.tr069client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class TR069FeedXML {

  private static HttpClientContext authenticate() throws MalformedURLException {
    CredentialsProvider credsProvider = new BasicCredentialsProvider();
    credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials("tr-069", "tr-069"));
    HttpClientContext context = HttpClientContext.create();
    context.setCredentialsProvider(credsProvider);
    return context;
  }

  public static String getRequest(String tr069Method) throws IOException {
    FileReader fr = new FileReader("customtest/" + tr069Method + ".xml");
    BufferedReader br = new BufferedReader(fr);
    String line = null;
    String request = "";
    while ((line = br.readLine()) != null) {
      request += line;
    }
    br.close();
    return request;
  }

  /**
   * This is a test class to allow you to simulate one single TR-069 client by
   * feeding the XML request by request through standard input. Speeding up the
   * feeding process may be considered at a later stage (feeding from files).
   * 
   * @param args
   */
  public static void main(String[] args) {
    try {      CloseableHttpClient client = HttpClients.createDefault();

      String url = "http://localhost:8080/xapstr069";
      HttpPost hp = new HttpPost(url);
      HttpResponse hr = null;
      HttpClientContext context = authenticate();
      hr = client.execute(hp, context);

      // The XML to be sent to the server
      String request = getRequest("Inform");
      if (request != null) {
        StringEntity requestEntity = new StringEntity(request, ContentType.create("text/xml", "ISO-8859-1"));
        hp.setEntity(requestEntity);
      }

      int  statusCode = hr.getStatusLine().getStatusCode();

      
      String response = EntityUtils.toString(hr.getEntity());

      System.out.println("StatusCode: " + statusCode);
      hp.releaseConnection();
      System.out.println("Response:\n" + response);
    } catch (Throwable t) {
      System.out.println("An error occurred: " + t);
    }
  }

  /*
   * Process: Read Inform.xml - send (show) - receive (show)
   */

}
