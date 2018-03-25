/*
    James Manager
    https://github.com/foilen/james-manager
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.james.manager.service;

import java.io.File;
import java.util.Collections;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.foilen.james.manager.config.EmailManagerConfig;
import com.foilen.james.manager.config.EmailManagerConfigDatabase;
import com.foilen.smalltools.event.EventCallback;
import com.foilen.smalltools.event.EventList;
import com.foilen.smalltools.filesystemupdatewatcher.handler.OneFileUpdateNotifyer;
import com.foilen.smalltools.filesystemupdatewatcher.handler.OneFileUpdateNotifyerHandler;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.JsonTools;

@Component
public class ConfigurationServiceImpl extends AbstractBasics implements ConfigurationService, OneFileUpdateNotifyerHandler {

    @Value("${CONFIG_FILE}")
    private String configFile;

    private EventList<EmailManagerConfig> updateEvent = new EventList<>();
    private EmailManagerConfig emailManagerConfig;

    @Override
    public void addConfigurationUpdateCallback(EventCallback<EmailManagerConfig> callback) {
        updateEvent.addCallback(callback);

        if (emailManagerConfig != null) {
            try {
                callback.handle(emailManagerConfig);
            } catch (Exception e) {
                logger.error("Problem executing the callback", e);
            }
        }
    }

    @Override
    public void fileUpdated(String fileName) {
        // Load
        EmailManagerConfig newConfig;
        try {
            logger.info("Loading {}", configFile);
            newConfig = JsonTools.readFromFile(configFile, EmailManagerConfig.class);
        } catch (Exception e) {
            logger.error("Could not load the config file", e);
            return;
        }

        // Check needed config
        try {
            EmailManagerConfigDatabase database = newConfig.getDatabase();
            AssertTools.assertNotNull(database, "Missing 'database' field");
            AssertTools.assertNotNull(database.getHostname(), "Missing 'database.hostname' field");
            AssertTools.assertNotNull(database.getDatabase(), "Missing 'database.database' field");
            AssertTools.assertNotNull(database.getUsername(), "Missing 'database.username' field");
            AssertTools.assertNotNull(database.getPassword(), "Missing 'database.password' field");

            if (newConfig.getAccounts() == null) {
                newConfig.setAccounts(Collections.emptyList());
            }
            if (newConfig.getDomains() == null) {
                newConfig.setDomains(Collections.emptyList());
            }
            if (newConfig.getRedirections() == null) {
                newConfig.setRedirections(Collections.emptyList());
            }
        } catch (Exception e) {
            logger.error("Problem with the config's file content", e);
            return;
        }

        // Set it
        emailManagerConfig = newConfig;

        // Callbacks
        updateEvent.dispatch(newConfig);
    }

    @Override
    public EmailManagerConfig getConfiguration() {
        return emailManagerConfig;
    }

    @PostConstruct
    public void init() {
        configFile = new File(configFile).getAbsolutePath();
        logger.info("Initializing the watcher for config file {}", configFile);
        @SuppressWarnings("resource")
        OneFileUpdateNotifyer notifyer = new OneFileUpdateNotifyer(configFile, this);
        notifyer.initAutoUpdateSystem();
    }

}
