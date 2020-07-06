/*
 * Copyright (c) 2020, ChenJun(powe4j@outlook.com).
 *  <p>
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.gnu.org/licenses/lgpl-3.0.html
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
 * @author CJ (jclazz@outlook.com)
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
