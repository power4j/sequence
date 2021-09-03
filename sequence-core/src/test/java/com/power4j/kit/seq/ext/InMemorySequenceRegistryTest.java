package com.power4j.kit.seq.ext;

import com.power4j.kit.seq.core.Sequence;
import com.power4j.kit.seq.persistent.Partitions;
import com.power4j.kit.seq.persistent.SeqHolder;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import com.power4j.kit.seq.persistent.provider.InMemorySeqSynchronizer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/9/2
 * @since 1.0
 */
public class InMemorySequenceRegistryTest {

	private final long initVal = 1L;

	private final int poolSize = 100;

	private final Supplier<String> partitionFunc = Partitions.DAILY;

	private SeqSynchronizer seqSynchronizer;

	protected Sequence<Long> createSeq(String name) {
		return createSeq(name, partitionFunc);
	}

	protected Sequence<Long> createSeq(String name, Supplier<String> partitionFunc) {
		return SeqHolder.builder().name(name).synchronizer(seqSynchronizer).partitionFunc(partitionFunc)
				.initValue(initVal).poolSize(poolSize)
				.seqFormatter((seqName, partition, value) -> String.format("%s.%s.%04d", seqName, partition, value))
				.build();
	}

	@Before
	public void setup() {
		seqSynchronizer = new InMemorySeqSynchronizer();
	}

	@Test
	public void simpleTest() {
		final String nameA100 = "A100";
		final String nameA200 = "A200";

		SequenceRegistry<Long, Sequence<Long>> registry = new InMemorySequenceRegistry<>();
		Sequence<Long> a100 = registry.get(nameA100).orElse(null);
		Assert.assertNull(a100);

		a100 = registry.getOrRegister(nameA100, this::createSeq);
		Assert.assertEquals(initVal, a100.next().longValue());

		registry.register(nameA200, createSeq(nameA200));
		long val = registry.get(nameA200).map(Sequence::next).orElse(-1L);
		Assert.assertEquals(initVal, val);

		Assert.assertEquals(2, registry.size());

		String v1 = registry.get(nameA100).map(Sequence::nextStr).orElse("");
		System.out.println(v1);
		Assert.assertTrue(v1.startsWith(nameA100));

		String v2 = registry.get(nameA200).map(Sequence::nextStr).orElse("");
		System.out.println(v2);
		Assert.assertTrue(v2.startsWith(nameA200));

		Assert.assertTrue(registry.remove(nameA100).isPresent());
	}

	@Test
	public void getOrRegisterTest() {
		SequenceRegistry<Long, Sequence<Long>> registry = new InMemorySequenceRegistry<>();
		String nameTemplate = "Biz_%03d";

		// 每个Sequence 使用一次
		for (int i = 0; i < 10; i++) {
			String name = String.format(nameTemplate, i);
			String val = registry.getOrRegister(name, this::createSeq).nextStr();
			System.out.println(val);
			String[] parts = val.split("\\.");
			Assert.assertEquals(1, Integer.parseInt(parts[2]));
		}

		// 每个Sequence 再使用一次
		for (int i = 0; i < 10; i++) {
			String name = String.format(nameTemplate, i);
			String val = registry.getOrRegister(name, this::createSeq).nextStr();
			System.out.println(val);
			String[] parts = val.split("\\.");
			Assert.assertEquals(2, Integer.parseInt(parts[2]));
		}
	}

	/**
	 * 分区计算函数按3取模,因此 ： <br/>
	 * 取号器每一次取号，区段都和上一次不同 <br/>
	 * 每3次又回到之前的号段,但因为重新批量取号的缘故，会出现跳号现象
	 */
	@Test
	public void partitionRoundRobinTest() {
		final int mod = 3;
		AtomicInteger count = new AtomicInteger();
		Supplier<String> rolling = () -> Integer.toString(count.getAndIncrement() % mod);
		SequenceRegistry<Long, Sequence<Long>> registry = new InMemorySequenceRegistry<>();
		String nameTemplate = "R%02d";

		List<String> results = new ArrayList<>(32);
		for (int i = 0; i < 3; i++) {
			String name = String.format(nameTemplate, i);
			for (int round = 0; round < 20; round++) {
				String val = registry.getOrRegister(name, o -> createSeq(o, rolling)).nextStr();
				results.add(val);
				String[] parts = val.split("\\.");
				long except = (round / mod) * poolSize + initVal;
				Assert.assertEquals(except, Integer.parseInt(parts[2]));
			}
		}
		// @formatter:off
		results.stream()
				.collect(Collectors.groupingBy(o -> o.substring(0, o.lastIndexOf('.'))))
				.entrySet()
				.stream()
				.sorted(java.util.Map.Entry.comparingByKey())
				.forEach(kv -> System.out.printf("%s : %s%n", kv.getKey(), String.join(" -> ", kv.getValue())));
        // @formatter:on

	}

}