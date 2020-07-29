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

import com.power4j.kit.seq.TestUtil;
import com.power4j.kit.seq.persistent.AddState;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
@Slf4j
public abstract class SynchronizerTestCase {

	protected abstract SeqSynchronizer getSeqSynchronizer();

	/**
	 * 一般使用步骤测试
	 */
	@Test
	public void simpleTest() {
		final SeqSynchronizer seqSynchronizer = getSeqSynchronizer();
		final String seqName = "power4j";
		final String partition = TestUtil.strNow();
		final long initValue = 1000L;
		final long newValue = 1L;
		seqSynchronizer.tryCreate(seqName, partition, initValue);
		Optional<Long> consume = seqSynchronizer.getNextValue(seqName, partition);
		Assert.assertTrue(consume.get().longValue() == initValue);

		boolean ret = seqSynchronizer.tryUpdate(seqName, partition, -1, newValue);
		Assert.assertFalse(ret);

		ret = seqSynchronizer.tryUpdate(seqName, partition, initValue, newValue);
		Assert.assertTrue(ret);

		consume = seqSynchronizer.getNextValue(seqName, partition);
		Assert.assertTrue(consume.get().longValue() == newValue);

		AddState addState = seqSynchronizer.tryAddAndGet(seqName, partition, -1, 0);
		Assert.assertTrue(addState.getCurrent() - addState.getPrevious() == -1);
		Assert.assertTrue(addState.getTotalOps() == 1);
	}

	/**
	 * 测试多线程更新操作
	 */
	@Test
	public void multipleThreadUpdateTest() {
		final SeqSynchronizer seqSynchronizer = getSeqSynchronizer();
		final String seqName = "power4j";
		final String partition = TestUtil.strNow();
		final long initValue = 1L;
		final long finalValue = 1000L;
		final int delta = 1;
		final int threads = 8;
		CountDownLatch threadReady = new CountDownLatch(threads);
		CountDownLatch threadDone = new CountDownLatch(threads);
		AtomicLong updateCount = new AtomicLong();
		ExecutorService executorService = Executors.newFixedThreadPool(threads);

		for (int t = 0; t < threads; ++t) {
			CompletableFuture.runAsync(() -> {
				threadReady.countDown();
				TestUtil.wait(threadReady);
				seqSynchronizer.tryCreate(seqName, partition, initValue);
				long current;
				int loop = 0;
				while ((current = seqSynchronizer.getNextValue(seqName, partition).get()) != finalValue) {
					if (loop % 100 == 0) {
						log.info(String.format("[thread %s] loop %08d, current = %08d",
								Thread.currentThread().getName(), loop, current));
					}
					++loop;
					seqSynchronizer.tryUpdate(seqName, partition, current, current + delta);
					updateCount.incrementAndGet();
					try {
						Thread.sleep(new Random().nextInt(3) + 1);
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				threadDone.countDown();
			}, executorService).exceptionally(e -> {
				threadDone.countDown();
				e.printStackTrace();
				return null;
			});
		}
		TestUtil.wait(threadDone);
		long lastValue = seqSynchronizer.getNextValue(seqName, partition).get();
		log.info(String.format("lastValue value = %d , update count = %d", lastValue, updateCount.get()));

		log.info(String.format("synchronizer query count = %d , update count = %d", seqSynchronizer.getQueryCounter(),
				seqSynchronizer.getUpdateCounter()));

		Assert.assertTrue(lastValue == finalValue);
	}

	/**
	 * 测试多线程加法操作
	 */
	@Test
	public void multipleThreadAddTest() {
		final SeqSynchronizer seqSynchronizer = getSeqSynchronizer();
		final String seqName = "power4j";
		final String partition = TestUtil.strNow();
		final long initValue = 1L;
		final long finalValue = 10000L;
		final int delta = 1;
		final int threads = 8;
		CountDownLatch threadReady = new CountDownLatch(threads);
		CountDownLatch threadDone = new CountDownLatch(threads);
		AtomicLong opCount = new AtomicLong();
		ExecutorService executorService = Executors.newFixedThreadPool(threads);

		for (int t = 0; t < threads; ++t) {
			CompletableFuture.runAsync(() -> {
				threadReady.countDown();
				TestUtil.wait(threadReady);
				seqSynchronizer.tryCreate(seqName, partition, initValue);
				int loop = 0;
				AddState addState;
				do {
					addState = seqSynchronizer.tryAddAndGet(seqName, partition, delta, 3);
					opCount.addAndGet(addState.getTotalOps());
					if (loop % 200 == 0) {
						log.info(String.format("[thread %s] loop %08d, from %08d to %08d",
								Thread.currentThread().getName(), loop, addState.getPrevious(), addState.getCurrent()));
						Assert.assertTrue(
								!addState.isSuccess() || addState.getCurrent() - addState.getPrevious() == delta);
					}
					++loop;
				}
				while (!addState.isSuccess() || addState.getCurrent() < finalValue);
				threadDone.countDown();
				log.info(String.format("[thread %s] [done] current = %08d", Thread.currentThread().getName(),
						addState.getCurrent()));
			}, executorService).exceptionally(e -> {
				threadDone.countDown();
				e.printStackTrace();
				return null;
			});
		}

		TestUtil.wait(threadDone);
		long lastValue = seqSynchronizer.getNextValue(seqName, partition).get();
		log.info(String.format("lastValue value = %d , operate count = %d", lastValue, opCount.get()));

		log.info(String.format("synchronizer query count = %d , update count = %d", seqSynchronizer.getQueryCounter(),
				seqSynchronizer.getUpdateCounter()));

		Assert.assertTrue(lastValue == finalValue + threads - 1);
	}

}
