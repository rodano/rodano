#!/usr/bin/env sh

### BACK-END
#compile project
mvn -f /workspaces/backend/pom.xml compile

#initialize database
/etc/init.d/mariadb start
mysql -e "alter user 'root'@'localhost' identified via mysql_native_password using password('root');"
cat <<EOT > ~/.my.cnf
[client]
user=root
password=root
EOT

mysql -uroot -proot -e "create database rodano;"
mvn -f backend/pom.xml spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=database,local -Drodano.init.with-data=true"


### FRONT-END
#install dependencies
npm --prefix /workspaces/frontends/main install
