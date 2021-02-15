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

import com.power4j.kit.seq.persistent.SeqSynchronizer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Endpoint for {@link SeqSynchronizer}
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/9/26
 * @since 1.4
 */
@Endpoint(id = "sequence-synchronizer")
public class SeqSynchronizerEndpoint implements SmartInitializingSingleton {

	private final ApplicationContext applicationContext;

	private Map<String, SeqSynchronizer> synchronizerBeanMap;

	public SeqSynchronizerEndpoint(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterSingletonsInstantiated() {
		synchronizerBeanMap = applicationContext.getBeansOfType(SeqSynchronizer.class);
	}

	@ReadOperation
	public List<SynchronizerInfo> synchronizerInfo() {

		// @formatter:off

		return synchronizerBeanMap.entrySet()
				.stream()
				.map(kv -> new SynchronizerInfo(
						kv.getKey(),
						kv.getValue().getClass().getName(),
						kv.getValue().getQueryCounter(),
						kv.getValue().getUpdateCounter()))
				.collect(Collectors.toList());

		// @formatter:on
	}

	@Getter
	@AllArgsConstructor
	public static class SynchronizerInfo {

		private final String beanName;

		private final String className;

		private final Long queryCount;

		private final Long updateCount;

	}

}
