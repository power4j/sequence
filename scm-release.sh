#!/bin/bash

export TEST_MYSQL_HOST=10.11.12.7
export TEST_MYSQL_PORT=3306
export TEST_MYSQL_USER=root
export TEST_MYSQL_PWD=root

mvn -B release:clean release:prepare release:perform