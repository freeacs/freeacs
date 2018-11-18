package com.github.freeacs.web.routes;

import static spark.Spark.get;
import static spark.Spark.post;

import com.github.freeacs.web.config.PasswordEncoder;
import com.github.freeacs.web.config.SecurityConfig;
import com.github.freeacs.web.config.UserService;
import com.github.freeacs.web.security.ThreadUser;
import com.github.freeacs.web.security.WebUser;
import freemarker.template.Configuration;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import javax.sql.DataSource;
import spark.ModelAndView;
import spark.Request;
import spark.RouteGroup;
import spark.template.freemarker.FreeMarkerEngine;

public class LoginRoute implements RouteGroup {
  private static final PasswordEncoder encoder = SecurityConfig.encoder();

  private final String ctxPath;
  private final Configuration configuration;
  private final DataSource mainDs;

  public LoginRoute(String ctxPath, Configuration configuration, DataSource mainDs) {
    this.ctxPath = ctxPath;
    this.configuration = configuration;
    this.mainDs = mainDs;
  }

  @Override
  public void addRoutes() {
    get("", (req, res) -> displayLogin(configuration, req));
    post(
        "",
        (req, res) -> {
          String username = req.raw().getParameter("username");
          String password = req.raw().getParameter("password");
          String csrf = req.raw().getParameter("csrf");
          if (csrf == null || !Objects.equals(req.session().attribute("csrf"), csrf)) {
            res.redirect(ctxPath + "/index");
            return null;
          }
          WebUser userDetails = UserService.loadUserByUsername(mainDs, username);
          if (Objects.equals(userDetails.getPassword(), encoder.encode(password))) {
            req.session(true).attribute("loggedIn", userDetails);
            ThreadUser.setUserDetails(userDetails);
            res.redirect(ctxPath + "/index");
            return null;
          }
          return displayLogin(configuration, req);
        });
  }

  private static String displayLogin(Configuration configuration, Request req) {
    String uuid = UUID.randomUUID().toString();
    req.session(true).attribute("csrf", uuid);
    return new FreeMarkerEngine(configuration)
        .render(
            new ModelAndView(
                new HashMap<String, String>() {
                  {
                    put("csrf", uuid);
                  }
                },
                "login.ftl"));
  }
}
