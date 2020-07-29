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

package com.power4j.kit.seq.persistent;

import com.power4j.kit.seq.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/14
 * @since 1.0
 */
@Slf4j
public abstract class SeqHolderTestCase {

	protected abstract SeqHolder getSeqHolder();

	@Test
	public void getValueTest() {
		final SeqHolder holder = getSeqHolder();
		final int threads = 8;
		final int loops = 100000;
		CountDownLatch threadReady = new CountDownLatch(threads);
		CountDownLatch threadDone = new CountDownLatch(threads);
		AtomicLong opCount = new AtomicLong();
		ExecutorService executorService = Executors.newFixedThreadPool(threads);
		log.info(String.format("start test loops = %d threads = %d", loops, threads));
		Instant startTime = Instant.now();
		for (int t = 0; t < threads; ++t) {
			CompletableFuture.runAsync(() -> {
				threadReady.countDown();
				Set<Long> dataSet = new HashSet<>(loops);
				TestUtil.wait(threadReady);

				for (int i = 0; i < loops; ++i) {
					if (i % 5000 == 0) {
						log.info(String.format("[thread %s] test running [%d / %d]", Thread.currentThread().getName(),
								i, loops));
					}
					long val = holder.next();
					dataSet.add(val);
				}
				int size = dataSet.size();
				dataSet.clear();
				opCount.addAndGet(size);
				threadDone.countDown();
				log.info(String.format("[thread %s] [done] dataset size = %08d", Thread.currentThread().getName(),
						size));
				Assert.assertEquals(size, loops);
			}, executorService).exceptionally(e -> {
				threadDone.countDown();
				e.printStackTrace();
				return null;
			});
		}

		TestUtil.wait(threadDone);
		long timeCost = Duration.between(startTime, Instant.now()).toMillis();
		log.info(String.format("test end,opCount = %d,time cost = %d ms", opCount.get(), timeCost));
		Assert.assertEquals(opCount.get(), loops * threads);
	}

}
