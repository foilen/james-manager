/*
    James Manager
    https://github.com/foilen/james-manager
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.james.manager;

public class JamesManagerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JamesManagerException(String message) {
        super(message);
    }

    public JamesManagerException(String message, Throwable cause) {
        super(message, cause);
    }

}
