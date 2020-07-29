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

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.power4j.kit.seq.utils.EnvUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.lettuce.core.RedisClient;

import javax.sql.DataSource;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
public class TestServices {

	/**
	 * protocol//[hosts][/database][?properties]
	 */
	private final static String DEFAULT_MYSQL_JDBC_URL = "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false";

	/**
	 * jdbc:postgresql://host:port/database
	 */
	private final static String DEFAULT_POSTGRESQL_JDBC_URL = "jdbc:postgresql://127.0.0.1:5432/test?ssl=false";

	/**
	 * redis://[password@]host [: port][/database]
	 */
	public final static String DEFAULT_REDIS_URI = "redis://127.0.0.1:6379";

	/**
	 * mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database.collection][?options]]
	 */
	public final static String DEFAULT_MONGO_URI = "mongodb://127.0.0.1:27017";

	public static DataSource getMySqlDataSource() {
		String jdbcUrl = EnvUtil.getStr("TEST_MYSQL_URL", DEFAULT_MYSQL_JDBC_URL);
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(jdbcUrl);
		config.setUsername(EnvUtil.getStr("TEST_MYSQL_USER", "root"));
		config.setPassword(EnvUtil.getStr("TEST_MYSQL_PWD", ""));
		return new HikariDataSource(config);
	}

	public static DataSource getPostgreSqlDataSource() {
		String jdbcUrl = EnvUtil.getStr("TEST_POSTGRESQL_URL", DEFAULT_POSTGRESQL_JDBC_URL);
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(jdbcUrl);
		config.setUsername(EnvUtil.getStr("TEST_POSTGRESQL_USER", "postgres"));
		config.setPassword(EnvUtil.getStr("TEST_POSTGRESQL_PWD", ""));
		return new HikariDataSource(config);
	}

	public static RedisClient getRedisClient() {
		String redisUri = EnvUtil.getStr("TEST_REDIS_URI", DEFAULT_REDIS_URI);
		RedisClient redisClient = RedisClient.create(redisUri);

		return redisClient;
	}

	public static MongoClient getMongoClient() {
		String mongoUri = EnvUtil.getStr("TEST_MONGO_URI", DEFAULT_MONGO_URI);
		MongoClient mongoClient = MongoClients.create(mongoUri);

		return mongoClient;
	}

}
