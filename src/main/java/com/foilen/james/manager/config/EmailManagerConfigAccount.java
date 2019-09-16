/*
    James Manager
    https://github.com/foilen/james-manager
    Copyright (c) 2018-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.james.manager.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.foilen.smalltools.tools.AbstractBasics;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailManagerConfigAccount extends AbstractBasics implements Comparable<EmailManagerConfigAccount> {

    private String email;
    private String password;
    private String passwordSha512;

    @Override
    public int compareTo(EmailManagerConfigAccount o) {
        return this.email.compareTo(o.email);
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordSha512() {
        return passwordSha512;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPasswordSha512(String passwordSha512) {
        this.passwordSha512 = passwordSha512;
    }

}
