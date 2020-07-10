/*
 * Copyright (c) 2020 ChenJun(power4j@outlook.com)
 * Sequence is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.power4j.kit.seq.spring.boot.autoconfigure;

import com.power4j.kit.seq.core.SeqFormatter;
import com.power4j.kit.seq.core.Sequence;
import com.power4j.kit.seq.persistent.Partitions;
import com.power4j.kit.seq.persistent.SeqHolder;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/6
 * @since 1.0
 */
@Configuration
@ComponentScan("com.power4j.kit.seq.spring.boot.autoconfigure")
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class SequenceAutoConfigure {

	@Bean
	@ConditionalOnMissingBean(value = Long.class, parameterizedContainer = Sequence.class)
	@ConditionalOnProperty(prefix = SequenceProperties.PREFIX, name = "enabled", havingValue = "true",
			matchIfMissing = true)
	public Sequence<Long> sequence(SequenceProperties sequenceProperties, SeqSynchronizer seqSynchronizer) {
		// 按月分区:即每个月有 Long.MAX 个序号可用
		SeqHolder holder = new SeqHolder(seqSynchronizer, sequenceProperties.getName(), Partitions.MONTHLY,
				sequenceProperties.getStartValue(), sequenceProperties.getFetchSize(), SeqFormatter.DEFAULT_FORMAT);
		return holder;
	}

}
