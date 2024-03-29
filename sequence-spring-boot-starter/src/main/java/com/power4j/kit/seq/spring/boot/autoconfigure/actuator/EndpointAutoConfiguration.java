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

package com.power4j.kit.seq.spring.boot.autoconfigure.actuator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * 监控端点自动配置
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/9/26
 * @since 1.4
 */
@AutoConfiguration
public class EndpointAutoConfiguration {

	@Autowired
	private ApplicationContext applicationContext;

	@Bean
	@ConditionalOnMissingBean(SequenceEndpoint.class)
	@ConditionalOnAvailableEndpoint(endpoint = SequenceEndpoint.class)
	public SequenceEndpoint sequenceEndpoint() {
		return new SequenceEndpoint(applicationContext);
	}

	@Bean
	@ConditionalOnMissingBean(SeqSynchronizerEndpoint.class)
	@ConditionalOnAvailableEndpoint(endpoint = SeqSynchronizerEndpoint.class)
	public SeqSynchronizerEndpoint seqSynchronizerEndpoint() {
		return new SeqSynchronizerEndpoint(applicationContext);
	}

}
