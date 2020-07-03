package com.power4j.kit.seq.persistent.provider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Mysql 测试
 * <p>
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
public class MySqlSynchronizerTest {

	public final static String SEQ_TABLE = "tb_seq";

	private MySqlSynchronizer mySqlSynchronizer;

	@Before
	public void prepare() {
		mySqlSynchronizer = new MySqlSynchronizer(SEQ_TABLE, TestDataSources.getMySqlDataSource());
		mySqlSynchronizer.createTable();
	}

	@After
	public void teardown() {
		mySqlSynchronizer.dropTable();
	}

	@Test
	public void simpleTest() {
		SynchronizerTestCase.simpleTest(mySqlSynchronizer);
	}

	@Test
	public void multipleThreadUpdateTest() {
		SynchronizerTestCase.multipleThreadUpdateTest(mySqlSynchronizer);
	}

	@Test
	public void multipleThreadAddTest() {
		SynchronizerTestCase.multipleThreadAddTest(mySqlSynchronizer);
	}

}