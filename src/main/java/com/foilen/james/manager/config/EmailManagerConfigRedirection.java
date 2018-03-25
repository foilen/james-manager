/*
    James Manager
    https://github.com/foilen/james-manager
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.james.manager.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailManagerConfigRedirection {

    private String email;
    private List<String> redirectTos = new ArrayList<>();

    public String getEmail() {
        return email;
    }

    public List<String> getRedirectTos() {
        return redirectTos;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRedirectTos(List<String> redirectTos) {
        this.redirectTos = redirectTos;
    }

}
