#!/bin/bash

mvn -s settings-ossrh.xml clean deploy -Dmaven.test.skip=true -P 'oss-release,aliyun-repo'