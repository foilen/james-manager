/*
    James Manager
    https://github.com/foilen/james-manager
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.james.manager;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.foilen.smalltools.tools.LogbackTools;
import com.foilen.smalltools.tools.SystemTools;
import com.google.common.base.Strings;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
public class JamesManagerApp {

    private final static Logger logger = LoggerFactory.getLogger(JamesManagerApp.class);

    public static void main(String[] args) throws Exception {

        try {

            // Get the parameters
            JamesManagerOptions options = new JamesManagerOptions();
            CmdLineParser cmdLineParser = new CmdLineParser(options);
            try {
                cmdLineParser.parseArgument(args);
            } catch (CmdLineException e) {
                e.printStackTrace();
                showUsage();
                return;
            }

            List<String> springBootArgs = new ArrayList<String>();
            if (options.debug) {
                springBootArgs.add("--debug");
            }

            // Get the configuration from options or environment
            String configFile = options.configFile;
            if (Strings.isNullOrEmpty(configFile)) {
                configFile = SystemTools.getPropertyOrEnvironment("CONFIG_FILE");
            }
            if (Strings.isNullOrEmpty(configFile)) {
                System.err.println("You need to provide a configuration file via the '--configFile' argument or the 'CONFIG_FILE' environment");
                showUsage();
                System.exit(1);
            }
            System.setProperty("CONFIG_FILE", configFile);

            // Set logging
            if (options.debug) {
                LogbackTools.changeConfig("/logback-debug.xml");
            } else {
                LogbackTools.changeConfig("/logback.xml");
            }

            String[] springBootArgsArray = springBootArgs.toArray(new String[springBootArgs.size()]);

            // Start the application
            logger.info("[MANAGER] Begin");

            SpringApplication application = new SpringApplication(JamesManagerApp.class);
            application.setBannerMode(Mode.OFF);
            application.run(springBootArgsArray);

            logger.info("[MANAGER] Started");

            // Check if debug
            if (options.debug) {
                LogbackTools.changeConfig("/logback-debug.xml");
            }

            // Stay up and running
            synchronized (application) {
                application.wait();
            }
        } catch (Exception e) {
            logger.error("Application failed", e);
            System.exit(1);
        }
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    private static void showUsage() {
        System.out.println("Usage:");
        CmdLineParser cmdLineParser = new CmdLineParser(new JamesManagerOptions());
        cmdLineParser.printUsage(System.out);
    }

}
