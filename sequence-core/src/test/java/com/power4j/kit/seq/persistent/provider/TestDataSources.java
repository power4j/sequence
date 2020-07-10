/*
 * Copyright (c) 2020 ChenJun(power4j@outlook.com)
 * Sequence is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.power4j.kit.seq.persistent.provider;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
public class TestDataSources {

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

	public static String getEnvOrDefault(String envKey, String defValue) {
		String val = System.getenv(envKey);
		return val != null ? val : defValue;
	}

}
