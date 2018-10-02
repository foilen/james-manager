# Create the MariaDB database

```
INSTANCE=tmp_db
DBNAME=tmp_db

docker run \
  --name $INSTANCE \
  -e MYSQL_ROOT_PASSWORD=ABC \
  -e DBNAME=$DBNAME \
  -p 3306:3306 \
  -d mariadb:10.3.6

```

# Create the database and tables

Enter in the Docker Instance:

```
docker exec -ti $INSTANCE /bin/bash
```

Inside the instance:

```
mysql -uroot -pABC
```

Inside MySql:

```
CREATE DATABASE IF NOT EXISTS tmp_db;
USE tmp_db;

CREATE TABLE IF NOT EXISTS `FOILEN_REDIRECTIONS` (
  `FROM_USER` varchar(100) NOT NULL,
  `FROM_DOMAIN` varchar(100) NOT NULL,
  `TO_EMAIL` varchar(100) NOT NULL
);
CREATE INDEX IF NOT EXISTS `FOILEN_REDIRECTIONS` ON `FOILEN_REDIRECTIONS` (FROM_DOMAIN, FROM_USER);

CREATE TABLE `JAMES_DOMAIN` (
  `DOMAIN_NAME` varchar(100) NOT NULL,
  PRIMARY KEY (`DOMAIN_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `JAMES_USER` (
  `USER_NAME` varchar(100) NOT NULL,
  `PASSWORD_HASH_ALGORITHM` varchar(100) NOT NULL,
  `PASSWORD` varchar(128) NOT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`USER_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
```

# Create config file

```
cat > _config.json << _EOF
{
  "database" : {
    "hostname" : "127.0.0.1",
    "port" : 3306,
    "database" : "tmp_db",
    "username" : "root",
    "password" : "ABC"
  },
  "domains" : [ "example.com", "example2.com" ],
  "accounts" : [ {
    "email" : "account1@example.com",
    "passwordSha512" : "ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f"
  }, {
    "email" : "account2@example.com",
    "passwordSha512" : "40a855bf0a93c1019d75dd5b59cd8157608811dd75c5977e07f3bc4be0cad98b22dde4db9ddb429fc2ad3cf9ca379fedf6c1dc4d4bb8829f10c2f0ee04a66663"
  }, {
    "email" : "account3@example.com",
    "password" : "qwerty"
  } ],
  "redirections" : [ {
    "email" : "redir1@example.com",
    "redirectTos" : [ "account1@example.com", "account1.1@example.com" ]
  }, {
    "email" : "*@example.com",
    "redirectTos" : [ "account2@example.com" ]
  }, {
    "email" : "yo@example.com",
    "redirectTos" : [ "account2@example.com", "account3@example.com" ]
  } ]
}
_EOF
```
