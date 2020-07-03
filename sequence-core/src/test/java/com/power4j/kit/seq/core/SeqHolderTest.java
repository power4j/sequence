package com.power4j.kit.seq.core;

import com.power4j.kit.seq.persistent.provider.MySqlSynchronizer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.time.LocalDateTime;

public class SeqHolderTest {

	private final static String SEQ_TABLE = "tb_seq";

	private final static String JDBC_URL = "jdbc:mysql://localhost:3306/seq_test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false";

	private MySqlSynchronizer mySqlSynchronizer;

	private static DataSource getDataSource() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(JDBC_URL);
		config.setUsername("root");
		config.setPassword("root");
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "100");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		return new HikariDataSource(config);
	}

	@Before
	public void prepare() {
		mySqlSynchronizer = new MySqlSynchronizer(SEQ_TABLE, getDataSource());
		mySqlSynchronizer.createTable();
	}

	@After
	public void teardown() {
		mySqlSynchronizer.dropTable();
	}

	@Test
	public void simpleTest() {
		final String seqName = "power4j";
		final long initValue = 1000L;
		final int size = 10;
		SeqHolder holder = new SeqHolder(mySqlSynchronizer, seqName, () -> LocalDateTime.now().toString(), initValue,
				size);
		for (int loop = 0; loop < 10; ++loop) {
			for (int i = 0; i < size; ++i) {
				System.out.println(holder.nextFormatted().get());
			}

			System.out.println(String.format("pull count = %d", holder.getPullCount()));
			Assert.assertTrue(holder.getPullCount() == loop + 1);
		}
	}

}