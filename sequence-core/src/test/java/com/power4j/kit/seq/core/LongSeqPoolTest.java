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

package com.power4j.kit.seq.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class LongSeqPoolTest {

	private final String poolName = "test-seq-pool";

	@Test
	public void minValueTest() {
		long start = LongSeqPool.MIN_VALUE;
		int size = 2;
		LongSeqPool pool = LongSeqPool.forSize(poolName, start, size, false);
		Assert.assertEquals(pool.minValue().longValue(), start);
		Assert.assertEquals(pool.capacity(), size);
		Assert.assertEquals(pool.remaining(), size);
		Assert.assertEquals(pool.peek().longValue(), start);
		for (int i = 0; i < size; ++i) {
			long val = pool.next();
			Assert.assertEquals(pool.capacity(), size);
			Assert.assertEquals(pool.remaining(), size - (i + 1));
			Assert.assertEquals(val, start + i);
		}
		Assert.assertEquals(pool.remaining(), 0);
	}

	@Test
	public void maxValueTest() {
		long start = LongSeqPool.MAX_VALUE;
		int size = 1;
		LongSeqPool pool = LongSeqPool.forSize(poolName, start, size, false);
		Assert.assertEquals(pool.minValue().longValue(), start);
		Assert.assertEquals(pool.maxValue().longValue(), start);
		Assert.assertEquals(pool.capacity(), size);
		Assert.assertEquals(pool.remaining(), size);
		Assert.assertEquals(pool.peek().longValue(), start);
		for (int i = 0; i < size; ++i) {
			long val = pool.next();
			Assert.assertEquals(pool.capacity(), size);
			Assert.assertEquals(pool.remaining(), size - (i + 1));
			Assert.assertEquals(val, start + i);
		}
		Assert.assertEquals(pool.remaining(), 0);
	}

	@Test
	public void simpleTest() {
		final long start = 0L;
		final int size = 200;
		LongSeqPool pool = LongSeqPool.forSize(poolName, start, size, false);

		Assert.assertEquals(pool.capacity(), size);
		Assert.assertEquals(pool.remaining(), size);
		Assert.assertEquals(pool.peek().longValue(), start);

		for (int i = 0; i < size; ++i) {
			long val = pool.take();
			int taken = i + 1;
			log.info(String.format("#[%04d] get = %d, peek next = %d, remaining = %d", taken, val, pool.peek(),
					pool.remaining()));
			Assert.assertEquals(pool.capacity(), size);
			Assert.assertEquals(pool.remaining(), size - taken);
			Assert.assertEquals(val, start + i);
		}
	}

	@Test
	public void rollingTest() {
		final long start = 0L;
		final int size = 20;
		LongSeqPool pool = LongSeqPool.forSize(poolName, start, size, true);

		for (int round = 0; round < 3; ++round) {
			for (int i = 0; i < size; ++i) {
				long val = pool.take();
				int taken = i + 1;
				log.info(String.format("#[%04d] get = %d, peek next = %d, remaining = %d", taken, val, pool.peek(),
						pool.remaining()));
				Assert.assertEquals(pool.capacity(), size);
				Assert.assertEquals(pool.remaining(), size);
				Assert.assertEquals(val, start + i);
			}
		}
	}

	@Test
	public void forRangeTest() {
		LongSeqPool pool = LongSeqPool.forRange(poolName, 1, 2, false);
		Assert.assertEquals(pool.capacity(), 2);

		Optional<Long> val = pool.nextOpt();
		Assert.assertTrue(val.isPresent());

		val = pool.nextOpt();
		Assert.assertTrue(val.isPresent());

		val = pool.nextOpt();
		Assert.assertFalse(val.isPresent());
	}

	@Test
	public void forkTest() {
		final long start = 10L;
		final int size = 20;
		LongSeqPool pool = LongSeqPool.forSize(poolName, start, size, false);

		for (int i = 0; i < size; ++i) {
			LongSeqPool fork = pool.fork(poolName);
			Assert.assertEquals(pool.peek(), fork.peek());
			long val1 = pool.take();
			long val2 = fork.take();
			Assert.assertEquals(val1, val2);
			Assert.assertEquals(pool.remaining(), fork.remaining());
		}
	}

	@Test
	public void threadSafetyTest() {

		final int threads = Runtime.getRuntime().availableProcessors() * 2 + 1;
		final long start = 0L;
		final int size = 2000;
		LongSeqPool pool = LongSeqPool.forSize(poolName, start, size, true);
		Map<String, List<Long>> threadResults = new ConcurrentHashMap<>(threads);
		final CountDownLatch startRun = new CountDownLatch(threads);
		final CountDownLatch completed = new CountDownLatch(threads);
		final ExecutorService executorService = Executors.newFixedThreadPool(threads);

		// N 个线程并发使用同一个 LongSeqPool 对象
		for (int thread = 0; thread < threads; ++thread) {
			CompletableFuture.runAsync(() -> {
				List<Long> list = new ArrayList<>(size);
				startRun.countDown();
				try {
					startRun.await();
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				for (int i = 0; i < size; ++i) {
					try {
						Thread.sleep(new Random().nextInt(3));
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					list.add(pool.take());
				}
				threadResults.put(Thread.currentThread().getName(), list);
				completed.countDown();
			}, executorService);
		}
		try {
			completed.await();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		threadResults.entrySet().forEach(kv -> {
			Set<Long> distinct = kv.getValue().stream().collect(Collectors.toSet());
			// 采样数量一样，但是消费能力不一样，因此去重后数量有所不同
			log.info(String.format("thread[%s] size = %d,distinct = %d", kv.getKey(), kv.getValue().size(),
					distinct.size()));
		});

		// 把所有线程的采样数据聚合 -> 排序 -> 根据线程数量分片 -> 每个分片的数量应该相同
		List<Long> all = threadResults.values().stream().flatMap(List::stream).collect(Collectors.toList());
		// 每个线程执行了 N 次 next
		Assert.assertEquals(all.size(), threads * size);
		all.sort(Long::compareTo);
		List<List<Long>> bad = splitCollection(all, threads).stream().filter(list -> list.size() != threads)
				.collect(Collectors.toList());

		bad.forEach(list -> System.out
				.println("发现异常数据: " + list.stream().map(v -> v.toString()).collect(Collectors.joining(", "))));
		Assert.assertEquals(bad.size(), 0);
	}

	public static <T> List<List<T>> splitCollection(Collection<T> collection, int size) {
		final List<List<T>> result = new ArrayList<>();

		ArrayList<T> subList = new ArrayList<>(size);
		for (T t : collection) {
			if (subList.size() >= size) {
				result.add(subList);
				subList = new ArrayList<>(size);
			}
			subList.add(t);
		}
		result.add(subList);
		return result;
	}

}