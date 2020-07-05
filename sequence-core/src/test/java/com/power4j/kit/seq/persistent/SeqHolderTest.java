/*
 * Copyright (c) 2020, ChenJun(powe4j@outlook.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
		mySqlSynchronizer.createTable();
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
		SeqHolder holder = new SeqHolder(mySqlSynchronizer, seqName, () -> LocalDateTime.now().toString(), initValue,
				size, null);
		for (int loop = 0; loop < 10; ++loop) {
			for (int i = 0; i < size; ++i) {
				System.out.println(holder.nextFormatted().get());
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
		for (int t = 0; t < threads; ++t) {
			CompletableFuture.runAsync(() -> {
				threadReady.countDown();
				wait(threadReady);
				for (int i = 0; i < size; ++i) {
					long val = holder.next().get();
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