package com.power4j.sequence.example;

import com.power4j.kit.seq.core.SeqFormatter;
import com.power4j.kit.seq.core.Sequence;
import com.power4j.kit.seq.ext.SequenceRegistry;
import com.power4j.kit.seq.persistent.Partitions;
import com.power4j.kit.seq.persistent.SeqHolder;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/9/2
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class SeqService {

	private final Supplier<String> partitionFunc = Partitions.DAILY;

	private final SeqSynchronizer seqSynchronizer;

	private final SequenceRegistry<Long, Sequence<Long>> sequenceRegistry;

	private final static SeqFormatter MY_FORMATTER = (seqName, partition, value) -> String.format("%s-%s-%04d", seqName,
			partition, value);

	public String getForName(String name) {
		return sequenceRegistry.getOrRegister(name, this::createSequence).nextStr();
	}

	private Sequence<Long> createSequence(String name) {
		return new SeqHolder(seqSynchronizer, name, partitionFunc, 1L, 100, MY_FORMATTER);
	}

}
