# About

This is an application to update the Apache James Email service by applying the config from a file. It is updating
the accounts and redirections directly in the Database whenever the configuration file changes. 

# Local Usage


## Compile

`./create-local-release.sh`

## Configure

```
TMPDIR=$(mktemp -d)

# Create configuration file
vi $TMPDIR/manager-config.json
{
  "database" : {
    "hostname" : "127.0.0.1",
    "database" : "db_database_email",
    "port" : 3306,
    "username" : "email",
    "password" : "ABC"
  },
  "domains" : [ "example.com" ],
  "accounts" : [ {
    "email" : "account1@example.com",
    "passwordSha512" : "ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f"
  }, {
    "email" : "account2@example.com",
    "password" : "efg"
  } ],
  "redirections" : [ {
    "email" : "redir1@example.com",
    "redirectTos" : [ "account1@example.com" ]
  }, {
    "email" : "regex:.*@example.com",
    "redirectTos" : [ "account2@example.com" ]
  } ]
}
```

## Execute

To see the help:
`java -jar build/libs/james-manager-master-SNAPSHOT.jar`

To execute:
`java -jar build/libs/james-manager-master-SNAPSHOT.jar --configFile $TMPDIR/manager-config.json`

## Important points

* For setting an email account password, you can provide it in clear text or in SHA-512 Hash.

# Create release

`./create-public-release.sh`

# Use with debian

```bash
echo "deb https://dl.bintray.com/foilen/debian stable main" | sudo tee /etc/apt/sources.list.d/foilen.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 379CE192D401AB61
sudo apt update
sudo apt install james-manager
```
