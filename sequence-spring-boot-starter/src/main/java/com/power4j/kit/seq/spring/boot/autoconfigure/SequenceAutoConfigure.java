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

package com.power4j.kit.seq.spring.boot.autoconfigure;

import com.power4j.kit.seq.core.SeqFormatter;
import com.power4j.kit.seq.core.Sequence;
import com.power4j.kit.seq.ext.InMemorySequenceRegistry;
import com.power4j.kit.seq.ext.SequenceRegistry;
import com.power4j.kit.seq.persistent.Partitions;
import com.power4j.kit.seq.persistent.SeqHolder;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 自动配置
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/6
 * @since 1.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(SequenceProperties.class)
@Import({ JdbcSynchronizerConfigure.class, RedisSynchronizerConfigure.class })
public class SequenceAutoConfigure {

	@Bean
	@ConditionalOnMissingBean(value = Long.class, parameterizedContainer = Sequence.class)
	@ConditionalOnProperty(prefix = SequenceProperties.PREFIX, name = "enabled", havingValue = "true",
			matchIfMissing = true)
	public Sequence<Long> sequence(SequenceProperties sequenceProperties, SeqSynchronizer seqSynchronizer) {
		log.info("Sequence create,Using {}", seqSynchronizer.getClass().getSimpleName());
		// 按月分区:即每个月有 Long.MAX 个序号可用

		// @formatter:off

		return SeqHolder.builder()
				.name(sequenceProperties.getName())
				.synchronizer(seqSynchronizer)
				.partitionFunc(Partitions.MONTHLY)
				.initValue(sequenceProperties.getStartValue())
				.poolSize(sequenceProperties.getFetchSize())
				.seqFormatter(SeqFormatter.DEFAULT_FORMAT)
				.build();

		// @formatter:on

	}

	@Bean
	@ConditionalOnMissingBean(SequenceRegistry.class)
	@ConditionalOnProperty(prefix = SequenceProperties.PREFIX, name = "enabled", havingValue = "true",
			matchIfMissing = true)
	public SequenceRegistry<Long, Sequence<Long>> sequenceRegistry() {
		return new InMemorySequenceRegistry<>();
	}

}
