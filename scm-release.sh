#!/bin/bash

export TEST_MYSQL_HOST=10.11.12.7
export TEST_MYSQL_PORT=3306
export TEST_MYSQL_USER=root
export TEST_MYSQL_PWD=root
export TEST_REDIS_HOST=10.11.12.7
export TEST_REDIS_PORT=6379

mvn release:clean -P 'oss-release,aliyun-repo' && \
mvn release:prepare -P 'oss-release,aliyun-repo' && \
mvn release:perform -P 'oss-release,aliyun-repo'