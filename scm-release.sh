#!/bin/bash

export TEST_MYSQL_URL="jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false"
export TEST_MYSQL_USER=root
export TEST_MYSQL_PWD=root
export TEST_POSTGRESQL_URL="jdbc:postgresql://127.0.0.1/test?ssl=false"
export TEST_POSTGRESQL_USER=root
export TEST_POSTGRESQL_PWD=root
export TEST_REDIS_URI="redis://127.0.0.1:6379"
export TEST_MONGO_URI="mongodb://root:root@127.0.0.1:27017"

mvn release:clean -P 'oss-release,aliyun-repo' && \
mvn release:prepare -P 'oss-release,aliyun-repo' && \
mvn release:perform -P 'oss-release,aliyun-repo'