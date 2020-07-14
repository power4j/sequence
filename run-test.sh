#!/bin/bash

export TEST_MYSQL_HOST=10.11.12.7
export TEST_MYSQL_PORT=3306
export TEST_MYSQL_USER=root
export TEST_MYSQL_PWD=root
export TEST_REDIS_HOST=10.11.12.7
export TEST_REDIS_PORT=6379

mvn test -DskipTests=false -B -P 'aliyun-repo,!oss-release,!travis-ci'