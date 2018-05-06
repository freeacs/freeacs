package com.owera.tr069client;

import java.io.IOException;
import java.net.BindException;
import java.net.MalformedURLException;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.owera.tr069client.monitor.Status;

public class HttpHandler {

  private String serialNumber;

  private int serialNumberInt;

  private static String[] urls;

  private HttpClient client;

  private static Random random = new Random(System.currentTimeMillis());

  private static Logger logger = Logger.getLogger(Session.class);

  public HttpHandler(Arguments args, Status status) {
    client = HttpClients.custom().setRetryHandler(new RetryHandler(status)).build();
  }

  private String chooseUrl(Arguments args) {
    if (urls == null) {
      urls = args.getProvUrl().split(",");
    }
    int index = serialNumberInt % urls.length;
    return urls[index];
  }

  public String send(String request, Arguments args, Status status, String action) throws IOException {

    long startSend = System.currentTimeMillis();
    HttpResponse hr = null;
    String url = chooseUrl(args);
    HttpPost hp = new HttpPost(url);
    if (request != null) {
      StringEntity requestEntity = new StringEntity(request, ContentType.create("text/xml", "ISO-8859-1"));
      hp.setEntity(requestEntity);
    }

    boolean retry = true;
    int statusCode = HttpStatus.SC_OK;
    int executionCount = 0;
    long execTime = 0;
    while (retry) {
      try {
        long start = System.currentTimeMillis();
        HttpClientContext context = authenticate();
        hr = client.execute(hp, context);
        statusCode = hr.getStatusLine().getStatusCode();
        execTime = System.currentTimeMillis() - start;
        logger.debug("The " + action + "-request execution time: " + execTime + " ms. (serialnumber: " + serialNumber + ")");
        retry = false;
      } catch (BindException be) { // BindException is NOT handled by the
                                   // RetryHandler!!
        long delay = Util.getRetrySleep(executionCount++);
        retry = true;
        try {
          status.incRetryOccured(status.getCurrentOperation());
          Thread.sleep(delay);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } catch (Throwable t) {
        retry = false;
      }
    }

    if (statusCode != HttpStatus.SC_OK) {
      if (statusCode != HttpStatus.SC_NO_CONTENT)
        logger.error("The HTTP response from the server is NOT ok: " + statusCode + " (serialnumber: " + serialNumber + ")");
      return "";
    }

    String response = EntityUtils.toString(hr.getEntity());
    if (response == null) {
      System.out.println(url);
      System.out.println(statusCode);
    } else {
      bitrateSleep(request, response, args);
    }
    hp.releaseConnection();
    long endSend = System.currentTimeMillis();
    logger.info("Time not spent on send/receive in send() " + ((endSend - startSend) - execTime) + " ms. (serialnumber: " + serialNumber + ")");
    return response;
  }

  private HttpClientContext authenticate() throws MalformedURLException {
    CredentialsProvider credsProvider = new BasicCredentialsProvider();
    credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials("000000-TR069TestClient-" + serialNumber, "001122334455"));
    HttpClientContext context = HttpClientContext.create();
    context.setCredentialsProvider(credsProvider);
    return context;
  }

  private void bitrateSleep(String request, String response, Arguments args) {
    if (args.getBitRate() != Integer.MAX_VALUE) {
      long sleepTm;
      int totalBits;
      if (request != null) {
        totalBits = (request.getBytes().length + response.getBytes().length) * 8;
        sleepTm = (long) (((float) totalBits / args.getBitRate()) * 1000);
      } else {
        totalBits = (response.getBytes().length) * 8;
        sleepTm = (long) (((float) totalBits / args.getBitRate()) * 1000);
      }
      sleepTm += (long) 1500f * (100000f / ((float) args.getBitRate()));
      if (sleepTm > 0) {
        try {
          sleepTm = randomizeSleep(sleepTm);
          Thread.sleep(sleepTm);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Let the sleep time change with +/- 33%, at least when there is some
   * significant sleep-intervall.
   */
  private long randomizeSleep(long sleep) {
    if (sleep > 3)
      return sleep;
    int maxRandomintervall = (int) sleep * 33 / 100;
    boolean positiv = random.nextBoolean();

    int randomIntervall = random.nextInt(maxRandomintervall + 1);
    if (positiv)
      return sleep + randomIntervall;
    else
      return sleep - randomIntervall;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public int getSerialNumberInt() {
    return serialNumberInt;
  }

  public void setSerialNumberInt(int serialNumberInt) {
    this.serialNumberInt = serialNumberInt;
  }

}
