/*
    James Manager
    https://github.com/foilen/james-manager
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.james.manager.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.mariadb.jdbc.MariaDbDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.foilen.james.manager.config.EmailManagerConfig;
import com.foilen.james.manager.config.EmailManagerConfigAccount;
import com.foilen.james.manager.config.EmailManagerConfigDatabase;
import com.foilen.james.manager.config.EmailManagerConfigRedirection;
import com.foilen.smalltools.event.EventCallback;
import com.foilen.smalltools.hash.HashSha512;
import com.foilen.smalltools.listscomparator.ListComparatorHandler;
import com.foilen.smalltools.listscomparator.ListsComparator;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StringTools;

@Component
public class UpdateJamesService extends AbstractBasics implements EventCallback<EmailManagerConfig> {

    @Autowired
    private ConfigurationService configurationService;

    @Override
    public void handle(EmailManagerConfig config) {

        if (config == null) {
            logger.error("No configuration to update");
            return;
        }

        try {

            logger.info("[BEGIN] Updating the James configuration");
            // Get the DB
            EmailManagerConfigDatabase databaseConfig = config.getDatabase();
            MariaDbDataSource dataSource = new MariaDbDataSource(databaseConfig.getHostname(), databaseConfig.getPort(), databaseConfig.getDatabase());
            dataSource.setUserName(databaseConfig.getUsername());
            dataSource.setPassword(databaseConfig.getPassword());
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // Update domains
            {
                List<String> existing = jdbcTemplate.queryForList("SELECT DOMAIN_NAME FROM JAMES_DOMAIN ORDER BY DOMAIN_NAME", String.class);
                List<String> desired = config.getDomains().stream().sorted().distinct().collect(Collectors.toList());
                logger.info("[DOMAIN] Got {} existing and {} desired", existing.size(), desired.size());

                ListsComparator.compareLists(existing, desired, new ListComparatorHandler<String, String>() {

                    @Override
                    public void both(String left, String right) {
                        logger.info("[DOMAIN] Keep {}", left);
                    }

                    @Override
                    public void leftOnly(String existing) {
                        logger.info("[DOMAIN] Delete {}", existing);
                        jdbcTemplate.update("DELETE FROM JAMES_DOMAIN WHERE DOMAIN_NAME = ?", existing);
                    }

                    @Override
                    public void rightOnly(String desired) {
                        logger.info("[DOMAIN] Add {}", desired);
                        jdbcTemplate.update("INSERT INTO JAMES_DOMAIN (DOMAIN_NAME) VALUES (?)", desired);
                    }
                });
            }

            // Update accounts
            config.getAccounts().forEach(account -> {
                if (account.getPasswordSha512() == null && account.getPassword() != null) {
                    account.setPasswordSha512(HashSha512.hashString(account.getPassword()));
                }
            });
            {
                List<EmailManagerConfigAccount> existing = jdbcTemplate.query("SELECT USER_NAME, PASSWORD FROM JAMES_USER ORDER BY USER_NAME", new RowMapper<EmailManagerConfigAccount>() {
                    @Override
                    public EmailManagerConfigAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
                        EmailManagerConfigAccount item = new EmailManagerConfigAccount();
                        item.setEmail(rs.getString(1));
                        item.setPasswordSha512(rs.getString(2));
                        return item;
                    }
                });
                List<EmailManagerConfigAccount> desired = config.getAccounts().stream().sorted((a, b) -> a.getEmail().compareTo(b.getEmail())).distinct().collect(Collectors.toList());
                logger.info("[ACCOUNT] Got {} existing and {} desired", existing.size(), desired.size());

                ListsComparator.compareLists(existing, desired, new ListComparatorHandler<EmailManagerConfigAccount, EmailManagerConfigAccount>() {

                    @Override
                    public void both(EmailManagerConfigAccount existing, EmailManagerConfigAccount desired) {
                        logger.info("[ACCOUNT] Keep {}", existing.getEmail());

                        if (!StringTools.safeEquals(existing.getPasswordSha512(), desired.getPasswordSha512()) && desired.getPasswordSha512() != null) {
                            logger.info("[ACCOUNT] Update password {}", existing.getEmail());
                            jdbcTemplate.update("UPDATE JAMES_USER SET PASSWORD = ?, VERSION = VERSION + 1 WHERE USER_NAME = ?", desired.getPasswordSha512(), desired.getEmail());
                        }
                    }

                    @Override
                    public void leftOnly(EmailManagerConfigAccount existing) {
                        logger.info("[ACCOUNT] Delete {}", existing.getEmail());
                        jdbcTemplate.update("DELETE FROM JAMES_USER WHERE USER_NAME = ?", existing.getEmail());
                    }

                    @Override
                    public void rightOnly(EmailManagerConfigAccount desired) {
                        logger.info("[ACCOUNT] Add {}", desired.getEmail());
                        jdbcTemplate.update("INSERT INTO JAMES_USER (USER_NAME, PASSWORD, PASSWORD_HASH_ALGORITHM, version) VALUES (?, ?, 'SHA-512', 1)", desired.getEmail(),
                                desired.getPasswordSha512());
                    }
                });
            }

            // Update redirections
            {
                List<EmailRedirectionParts> existing = jdbcTemplate.query("SELECT TARGET_ADDRESS, USER_NAME, DOMAIN_NAME FROM JAMES_RECIPIENT_REWRITE ORDER BY TARGET_ADDRESS, USER_NAME, DOMAIN_NAME",
                        new RowMapper<EmailRedirectionParts>() {
                            @Override
                            public EmailRedirectionParts mapRow(ResultSet rs, int rowNum) throws SQLException {
                                EmailRedirectionParts item = new EmailRedirectionParts();
                                item.setFromEmail(rs.getString(1));
                                item.setToUser(rs.getString(2));
                                item.setToDomain(rs.getString(3));
                                return item;
                            }
                        });
                List<EmailRedirectionParts> desired = new ArrayList<>();
                for (EmailManagerConfigRedirection redirection : config.getRedirections()) {
                    for (String to : redirection.getRedirectTos()) {
                        String[] toParts = to.split("@");
                        desired.add(new EmailRedirectionParts() //
                                .setFromEmail(redirection.getEmail()) //
                                .setToUser(toParts[0]) //
                                .setToDomain(toParts[1]));
                    }
                }
                desired = desired.stream().sorted().distinct().collect(Collectors.toList());
                logger.info("[REDIRECTION] Got {} existing and {} desired", existing.size(), desired.size());

                ListsComparator.compareLists(existing, desired, new ListComparatorHandler<EmailRedirectionParts, EmailRedirectionParts>() {

                    @Override
                    public void both(EmailRedirectionParts existing, EmailRedirectionParts desired) {
                        logger.info("[REDIRECTION] Keep {}", existing);
                    }

                    @Override
                    public void leftOnly(EmailRedirectionParts existing) {
                        logger.info("[REDIRECTION] Delete {}", existing);
                        jdbcTemplate.update("DELETE FROM JAMES_RECIPIENT_REWRITE WHERE TARGET_ADDRESS = ? AND USER_NAME = ? AND DOMAIN_NAME = ?", //
                                existing.getFromEmail(), existing.getToUser(), existing.getToDomain());
                    }

                    @Override
                    public void rightOnly(EmailRedirectionParts desired) {
                        logger.info("[REDIRECTION] Add {}", desired);
                        jdbcTemplate.update("INSERT INTO JAMES_RECIPIENT_REWRITE (TARGET_ADDRESS, USER_NAME, DOMAIN_NAME) VALUES (?, ?, ?)", //
                                desired.getFromEmail(), desired.getToUser(), desired.getToDomain());
                    }
                });
            }

        } catch (Exception e) {
            logger.error("Problem updating the James configuration in the Database", e);
        }

        logger.info("[END] Updating the James configuration");
    }

    @PostConstruct
    public void init() {
        configurationService.addConfigurationUpdateCallback(this);
    }

}