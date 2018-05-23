package com.github.freeacs.web.config;

import com.github.freeacs.web.app.Main;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", Main.servletMapping);
        registry.addViewController("/login").setViewName("login");
    }

}