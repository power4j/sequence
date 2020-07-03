package com.power4j.kit.seq.persistent.provider;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
public class TestDataSources {

	public final static String MYSQL_JDBC_URL = "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false";

	public static DataSource getMySqlDataSource() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(MYSQL_JDBC_URL);
		config.setUsername("root");
		config.setPassword("");
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "100");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		return new HikariDataSource(config);
	}

}
