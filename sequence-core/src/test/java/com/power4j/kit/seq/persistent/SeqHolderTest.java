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

package com.power4j.kit.seq.persistent;

import com.power4j.kit.seq.core.LongSeqPool;
import com.power4j.kit.seq.persistent.provider.MySqlSynchronizer;
import com.power4j.kit.seq.persistent.provider.TestDataSources;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class SeqHolderTest {

	public final static String SEQ_TABLE = "tb_seq";

	private MySqlSynchronizer mySqlSynchronizer;

	@Before
	public void prepare() {
		mySqlSynchronizer = new MySqlSynchronizer(SEQ_TABLE, TestDataSources.getMySqlDataSource());
		mySqlSynchronizer.createMissingTable();
	}

	@After
	public void teardown() {
		mySqlSynchronizer.dropTable();
	}

	@Test
	public void simpleTest() {
		final String seqName = "power4j";
		final long initValue = LongSeqPool.MIN_VALUE;
		final int size = 10;
		SeqHolder holder = new SeqHolder(mySqlSynchronizer, seqName, LocalDateTime.now().toString(), initValue, size,
				null);
		for (int loop = 0; loop < 10; ++loop) {
			for (int i = 0; i < size; ++i) {
				System.out.println(holder.nextStr());
			}
			System.out.println(String.format("pull count = %d", holder.getPullCount()));
			Assert.assertTrue(holder.getPullCount() == loop + 1);
		}
	}

	@Test
	public void threadTest() {
		final String seqName = "power4j";
		final long initValue = LongSeqPool.MIN_VALUE;
		final int size = 1000;
		final int threads = 8;
		CountDownLatch threadReady = new CountDownLatch(threads);
		CountDownLatch threadDone = new CountDownLatch(threads);
		ExecutorService executorService = Executors.newFixedThreadPool(threads);
		AtomicLong got = new AtomicLong();

		SeqHolder holder = new SeqHolder(mySqlSynchronizer, seqName, () -> LocalDateTime.now().toString(), initValue,
				size, null);
		holder.prepare();
		for (int t = 0; t < threads; ++t) {
			CompletableFuture.runAsync(() -> {
				threadReady.countDown();
				wait(threadReady);
				for (int i = 0; i < size; ++i) {
					long val = holder.next();
					got.incrementAndGet();
					Assert.assertTrue(val >= initValue);
				}
				threadDone.countDown();
			}, executorService).exceptionally(e -> {
				threadDone.countDown();
				e.printStackTrace();
				return null;
			});
		}
		wait(threadDone);
		System.out.println(String.format("pull count = %d , except = %d", got.get(), size * threads));

		System.out.println(String.format("synchronizer query count = %d , update count = %d",
				mySqlSynchronizer.getQueryCount(), mySqlSynchronizer.getUpdateCount()));

		Assert.assertTrue(got.get() == size * threads);
	}

	public static void wait(CountDownLatch countDownLatch) {
		try {
			countDownLatch.await();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}