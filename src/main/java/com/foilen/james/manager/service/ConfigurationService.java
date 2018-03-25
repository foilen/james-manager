/*
    James Manager
    https://github.com/foilen/james-manager
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.james.manager.service;

import com.foilen.james.manager.config.EmailManagerConfig;
import com.foilen.smalltools.event.EventCallback;

public interface ConfigurationService {

    void addConfigurationUpdateCallback(EventCallback<EmailManagerConfig> callback);

    EmailManagerConfig getConfiguration();

}
