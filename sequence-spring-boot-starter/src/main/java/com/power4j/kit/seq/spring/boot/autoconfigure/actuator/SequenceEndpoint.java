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

import com.power4j.kit.seq.core.Sequence;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoint for {@code Sequence}
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/9/26
 * @since 1.4
 */
@Endpoint(id = "sequence")
public class SequenceEndpoint implements SmartInitializingSingleton {

	private final ApplicationContext applicationContext;

	private List<SequenceInfo> sequenceInfoList;

	public SequenceEndpoint(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterSingletonsInstantiated() {
		sequenceInfoList = applicationContext.getBeansOfType(Sequence.class).entrySet().stream()
				.map(kv -> new SequenceInfo(kv.getKey(), kv.getValue().getClass().getName(), kv.getValue().getName()))
				.collect(Collectors.toList());
	}

	@ReadOperation
	public List<SequenceInfo> sequenceBeans() {
		return sequenceInfoList;
	}

	@Getter
	@AllArgsConstructor
	public static class SequenceInfo {

		private final String beanName;

		private final String className;

		private final String seqName;

	}

}
