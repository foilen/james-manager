/*
    James Manager
    https://github.com/foilen/james-manager
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.james.manager.service.lock;

public interface LockService {

    void executeIfGotLock(String lockName, Runnable runnable);

    void init();

}
