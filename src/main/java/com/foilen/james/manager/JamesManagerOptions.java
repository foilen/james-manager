/*
    James Manager
    https://github.com/foilen/james-manager
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.james.manager;

import org.kohsuke.args4j.Option;

/**
 * The arguments to pass to the application.
 */
public class JamesManagerOptions {

    @Option(name = "--debug", usage = "To log everything (default: false)")
    public boolean debug;

    @Option(name = "--configFile", usage = "The config file path (default: none since using the CONFIG_FILE environment variable)")
    public String configFile;

}
