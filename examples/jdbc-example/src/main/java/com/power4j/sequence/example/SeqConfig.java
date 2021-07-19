package com.power4j.sequence.example;

import com.power4j.kit.seq.core.SeqFormatter;
import com.power4j.kit.seq.core.Sequence;
import com.power4j.kit.seq.persistent.Partitions;
import com.power4j.kit.seq.persistent.SeqHolder;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import com.power4j.kit.seq.spring.boot.autoconfigure.SequenceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/7/19
 * @since 1.0
 */
@Slf4j
@Configuration
public class SeqConfig {

	private final static SeqFormatter MY_FORMATTER = (seqName, partition, value) -> partition + "批次,第" + value + "号";

	@Bean
	public Sequence<Long> sequence(SequenceProperties sequenceProperties, SeqSynchronizer seqSynchronizer) {
		log.info("开始自定义配置,底层实现为:{}", seqSynchronizer.getClass().getSimpleName());
		// Partitions.MONTHLY 是分区函数,它返回分区的名称,在同一个分区中,序号自增(但不保证连续,比如服务重启,因为应用层是批量取号)
		// MY_FORMATTER 是格式化函数,应用层可以根据 seqName, partition, value 自定义输出
		return new SeqHolder(seqSynchronizer, sequenceProperties.getName(), Partitions.MONTHLY,
				sequenceProperties.getStartValue(), sequenceProperties.getFetchSize(), MY_FORMATTER);
	}

}
