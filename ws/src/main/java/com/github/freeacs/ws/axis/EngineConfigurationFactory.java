package com.github.freeacs.ws.axis;

import org.apache.axis.AxisProperties;
import org.apache.axis.ConfigurationException;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.EngineConfigurationFactoryDefault;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.server.AxisServer;
import org.apache.axis.utils.ClassUtils;
import org.apache.axis.utils.Messages;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.InputStream;

public class EngineConfigurationFactory extends EngineConfigurationFactoryDefault {

    private ServletConfig cfg;

    public static org.apache.axis.EngineConfigurationFactory newFactory(Object param) {
        // see comments inside method newFactory in org.apache.axis.configuration.EngineConfigurationFactoryServlet
        return (param instanceof ServletConfig)
                ? new EngineConfigurationFactory((ServletConfig) param)
                : null;
    }

    private EngineConfigurationFactory(ServletConfig cfg) {
        super();
        this.cfg = cfg;
    }

    /**
     * This method works similarly to the method defined in
     * org.apache.axis.configuration.EngineConfigurationFactoryServlet
     * except it also tries to find wsdd config file in classpath.
     *
     * @return instance of EngineConfiguration
     */
    @Override
    public EngineConfiguration getServerEngineConfig() {
        ServletContext ctx = cfg.getServletContext();

        // Respect the system property setting for a different config file
        String configFile = cfg.getInitParameter(OPTION_SERVER_CONFIG_FILE);
        if (configFile == null)
            configFile =
                    AxisProperties.getProperty(OPTION_SERVER_CONFIG_FILE);
        if (configFile == null) {
            configFile = SERVER_CONFIG_FILE;
        }

        /*
         * Use the WEB-INF directory
         * (so the config files can't get snooped by a browser)
         */
        String appWebInfPath = "/WEB-INF";

        FileProvider config = null;

        String realWebInfPath = ctx.getRealPath(appWebInfPath);

        /**
         * If path/file doesn't exist, it may still be accessible
         * as a resource-stream (i.e. it may be packaged in a JAR
         * or WAR file).
         */
        if (realWebInfPath == null ||
                !(new File(realWebInfPath, configFile)).exists()) {

            String name = appWebInfPath + "/" + configFile;
            InputStream is = ctx.getResourceAsStream(name);
            if (is != null) {
                // FileProvider assumes responsibility for 'is':
                // do NOT call is.close().
                config = new FileProvider(is);
            }

            // checking if file is available on class path
            name = configFile;
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            is = cl.getResourceAsStream(name);
            if (is != null) {
                config = new FileProvider(is);
            }

            if (config == null) {
                log.error(Messages.getMessage("servletEngineWebInfError03",
                        name));
            }
        }

        /**
         * Couldn't get data  OR  file does exist.
         * If we have a path, then attempt to either open
         * the existing file, or create an (empty) file.
         */
        if (config == null && realWebInfPath != null) {
            try {
                config = new FileProvider(realWebInfPath, configFile);
            } catch (ConfigurationException e) {
                log.error(Messages.getMessage("servletEngineWebInfError00"), e);
            }
        }

        /**
         * Fall back to config file packaged with AxisEngine
         */
        if (config == null) {
            log.warn(Messages.getMessage("servletEngineWebInfWarn00"));
            try {
                InputStream is =
                        ClassUtils.getResourceAsStream(AxisServer.class,
                                SERVER_CONFIG_FILE);
                config = new FileProvider(is);
            } catch (Exception e) {
                log.error(Messages.getMessage("servletEngineWebInfError02"), e);
            }
        }

        return config;
    }


}
