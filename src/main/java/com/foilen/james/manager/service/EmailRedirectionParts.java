/*
    James Manager
    https://github.com/foilen/james-manager
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.james.manager.service;

import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.collect.ComparisonChain;

public class EmailRedirectionParts extends AbstractBasics implements Comparable<EmailRedirectionParts> {

    private String fromEmail;
    private String toUser;
    private String toDomain;

    @Override
    public int compareTo(EmailRedirectionParts o) {
        return ComparisonChain.start() //
                .compare(this.fromEmail, o.fromEmail) //
                .compare(this.toUser, o.toUser) //
                .compare(this.toDomain, o.toDomain) //
                .result();
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public String getToDomain() {
        return toDomain;
    }

    public String getToUser() {
        return toUser;
    }

    public EmailRedirectionParts setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
        return this;
    }

    public EmailRedirectionParts setToDomain(String toDomain) {
        this.toDomain = toDomain;
        return this;
    }

    public EmailRedirectionParts setToUser(String toUser) {
        this.toUser = toUser;
        return this;
    }

}
