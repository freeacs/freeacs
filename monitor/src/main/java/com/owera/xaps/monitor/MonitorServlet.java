package com.owera.xaps.monitor;

import com.github.freeacs.common.scheduler.ExecutorWrapper;
import com.github.freeacs.common.ssl.EasySSLProtocolSocketFactory;
import com.github.freeacs.common.util.Sleep;
import com.owera.xaps.monitor.task.ModuleMonitorTask;
import com.owera.xaps.monitor.task.MonitorInfo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorServlet extends HttpServlet {
  private static final long serialVersionUID = 3051630277238752841L;

  private final Properties properties;
  private final ExecutorWrapper executorWrapper;

  private Configuration config;

  private static Logger log = LoggerFactory.getLogger(MonitorServlet.class);

  public MonitorServlet(Properties properties, ExecutorWrapper executorWrapper) {
    this.properties = properties;
    this.executorWrapper = executorWrapper;
    ProtocolSocketFactory socketFactory = new EasySSLProtocolSocketFactory();
    Protocol https = new Protocol("https", socketFactory, 443);
    Protocol.registerProtocol("https", https);
  }

  @Override
  public void init(ServletConfig serlvetConfig) throws ServletException {
    try {
      config = new Freemarker().initFreemarker();
      // Run at every 5 minute - light task - run module monitoring (check OK-servlet response for
      // all modules)
      ModuleMonitorTask moduleMonitorTask = new ModuleMonitorTask("ModuleMonitorTask", properties);
      executorWrapper.scheduleCron(
          "0 * * ? * * *",
          (tms) ->
              () -> {
                moduleMonitorTask.setThisLaunchTms(tms);
                moduleMonitorTask.run();
              });
    } catch (Exception ex) {
      log.error("Error while initializing Monitor: " + ex.getLocalizedMessage(), ex);
      throw new ServletException(ex);
    }
  }

  @Override
  public void destroy() {
    Sleep.terminateApplication();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    processRequest(request, response);
  }

  public void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    PrintWriter out = response.getWriter();
    Template page = config.getTemplate("main.ftl");
    Map<String, Object> rootMap = new HashMap<>();
    String async = request.getParameter("async");
    if (async != null) {
      rootMap.put("async", async);
    }
    List<MonitorInfo> events = new ArrayList<>();
    for (MonitorInfo mi : ModuleMonitorTask.getMonitorInfoSet()) {
      if ("monitor".equals(mi.getModule())) {
        continue;
      }
      events.add(mi);
    }
    rootMap.put("events", events);
    try {
      String embedded = request.getParameter("html");
      if ("no".equalsIgnoreCase(embedded)) {
        page.process(rootMap, out);
      } else {
        Template index = config.getTemplate("index.ftl");
        rootMap.put("main", "main.ftl");
        index.process(rootMap, out);
      }
    } catch (TemplateException e) {
      log.error("A template exception occured: " + e.getLocalizedMessage(), e);
      out.println(e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    processRequest(request, response);
  }
}
