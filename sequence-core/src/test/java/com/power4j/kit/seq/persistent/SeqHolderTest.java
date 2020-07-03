package com.power4j.kit.seq.persistent;

import com.power4j.kit.seq.persistent.provider.MySqlSynchronizer;
import com.power4j.kit.seq.persistent.provider.TestDataSources;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

public class SeqHolderTest {

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