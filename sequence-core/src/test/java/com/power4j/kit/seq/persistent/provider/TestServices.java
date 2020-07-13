/*
 * Copyright 2020 ChenJun (power4j@outlook.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.power4j.kit.seq.persistent.provider;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

import javax.sql.DataSource;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
public class TestServices {

	public final static String MYSQL_JDBC_URL_TEMPLATE = "jdbc:mysql://%s:%s/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false";

	public static DataSource getMySqlDataSource() {
		String jdbcUrl = String.format(MYSQL_JDBC_URL_TEMPLATE, getEnvOrDefault("TEST_MYSQL_HOST", "127.0.0.1"),
				getEnvOrDefault("TEST_MYSQL_PORT", "3306"));
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(jdbcUrl);
		config.setUsername(getEnvOrDefault("TEST_MYSQL_USER", "root"));
		config.setPassword(getEnvOrDefault("TEST_MYSQL_PWD", ""));
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "100");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		return new HikariDataSource(config);
	}

	public static RedisClient getRedisClient() {
		RedisURI redisUri = RedisURI.builder().withHost(getEnvOrDefault("TEST_REDIS_HOST", "127.0.0.1"))
				.withPort(getEnvOrDefault("TEST_REDIS_PORT", 6379)).build();
		RedisClient redisClient = RedisClient.create(redisUri);

		return redisClient;
	}

	public static String getEnvOrDefault(String envKey, String defValue) {
		String val = System.getenv(envKey);
		return val != null ? val : defValue;
	}

	public static Integer getEnvOrDefault(String envKey, Integer defValue) {
		String val = System.getenv(envKey);
		if (val == null) {
			return defValue;
		}
		try {
			return Integer.parseInt(val);
		}
		catch (NumberFormatException e) {
			return defValue;
		}
	}

}
