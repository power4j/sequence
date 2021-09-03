package com.power4j.kit.seq.ext;

import com.power4j.kit.seq.core.Sequence;
import com.power4j.kit.seq.persistent.Partitions;
import com.power4j.kit.seq.persistent.SeqHolder;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import com.power4j.kit.seq.persistent.provider.InMemorySeqSynchronizer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Supplier;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/9/2
 * @since 1.0
 */
public class InMemorySequenceRegistryTest {

	private final long initVal = 1L;

	private final Supplier<String> partitionFunc = Partitions.DAILY;

	private SeqSynchronizer seqSynchronizer;

	protected Sequence<Long> createSeq(String name) {
		return new SeqHolder(seqSynchronizer, name, partitionFunc, initVal, 100,
				(seqName, partition, value) -> String.format("%s.%s.%04d", seqName, partition, value));
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

}