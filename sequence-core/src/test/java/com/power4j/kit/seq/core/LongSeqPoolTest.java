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

package com.power4j.kit.seq.core;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class LongSeqPoolTest {

	private final String poolName = "test-seq-pool";

	@Test
	public void minValueTest() {
		long start = LongSeqPool.MIN_VALUE;
		int size = 2;
		LongSeqPool pool = LongSeqPool.forSize(poolName, start, size, false);
		assertEquals(pool.minValue().longValue(), start);
		assertEquals(pool.capacity(), size);
		assertEquals(pool.remaining(), size);
		assertEquals(pool.peek().longValue(), start);
		for (int i = 0; i < size; ++i) {
			long val = pool.next();
			assertEquals(pool.capacity(), size);
			assertEquals(pool.remaining(), size - (i + 1));
			assertEquals(val, start + i);
		}
		assertEquals(pool.remaining(), 0);
	}

	@Test
	public void maxValueTest() {
		long start = LongSeqPool.MAX_VALUE;
		int size = 1;
		LongSeqPool pool = LongSeqPool.forSize(poolName, start, size, false);
		assertEquals(pool.minValue().longValue(), start);
		assertEquals(pool.maxValue().longValue(), start);
		assertEquals(pool.capacity(), size);
		assertEquals(pool.remaining(), size);
		assertEquals(pool.peek().longValue(), start);
		for (int i = 0; i < size; ++i) {
			long val = pool.next();
			assertEquals(pool.capacity(), size);
			assertEquals(pool.remaining(), size - (i + 1));
			assertEquals(val, start + i);
		}
		assertEquals(pool.remaining(), 0);
	}

	@Test
	public void simpleTest() {
		final long start = 0L;
		final int size = 200;
		LongSeqPool pool = LongSeqPool.forSize(poolName, start, size, false);

		assertEquals(pool.capacity(), size);
		assertEquals(pool.remaining(), size);
		assertEquals(pool.peek().longValue(), start);

		for (int i = 0; i < size; ++i) {
			long val = pool.take();
			int taken = i + 1;
			System.out.println(String.format("#[%04d] get = %d, peek next = %d, remaining = %d", taken, val,
					pool.peek(), pool.remaining()));
			assertEquals(pool.capacity(), size);
			assertEquals(pool.remaining(), size - taken);
			assertEquals(val, start + i);
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
				System.out.println(String.format("#[%04d] get = %d, peek next = %d, remaining = %d", taken, val,
						pool.peek(), pool.remaining()));
				assertEquals(pool.capacity(), size);
				assertEquals(pool.remaining(), size);
				assertEquals(val, start + i);
			}
		}
	}

	@Test
	public void forRangeTest() {
		LongSeqPool pool = LongSeqPool.forRange(poolName, 1, 2, false);
		assertEquals(pool.capacity(), 2);

		Optional<Long> val = pool.nextOpt();
		assertTrue(val.isPresent());

		val = pool.nextOpt();
		assertTrue(val.isPresent());

		val = pool.nextOpt();
		assertFalse(val.isPresent());
	}

	@Test
	public void forkTest() {
		final long start = 10L;
		final int size = 20;
		LongSeqPool pool = LongSeqPool.forSize(poolName, start, size, false);

		for (int i = 0; i < size; ++i) {
			LongSeqPool fork = pool.fork(poolName);
			assertEquals(pool.peek(), fork.peek());
			long val1 = pool.take();
			long val2 = fork.take();
			assertEquals(val1, val2);
			assertEquals(pool.remaining(), fork.remaining());
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
			System.out.println(String.format("thread[%s] size = %d,distinct = %d", kv.getKey(), kv.getValue().size(),
					distinct.size()));
		});

		// 把所有线程的采样数据聚合 -> 排序 -> 根据线程数量分片 -> 每个分片的数量应该相同
		List<Long> all = threadResults.values().stream().flatMap(List::stream).collect(Collectors.toList());
		// 每个线程执行了 N 次 next
		assertEquals(all.size(), threads * size);
		all.sort(Long::compareTo);
		List<List<Long>> bad = splitCollection(all, threads).stream().filter(list -> list.size() != threads)
				.collect(Collectors.toList());

		bad.forEach(list -> System.out
				.println("发现异常数据: " + list.stream().map(v -> v.toString()).collect(Collectors.joining(", "))));
		assertEquals(bad.size(), 0);
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