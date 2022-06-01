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

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import com.power4j.kit.seq.persistent.provider.SimpleMongoSynchronizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/20
 * @since 1.0
 */
@AutoConfiguration
@ConditionalOnClass(MongoClient.class)
public class MongoSynchronizerConfigure {

	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = SequenceProperties.PREFIX, name = "backend", havingValue = "mongo")
	public MongoClient mongoClient(SequenceProperties sequenceProperties) {
		return MongoClients.create(sequenceProperties.getMongoUri());
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(MongoClient.class)
	public SeqSynchronizer mongoSynchronizer(SequenceProperties sequenceProperties, MongoClient mongoClient) {
		SimpleMongoSynchronizer synchronizer = new SimpleMongoSynchronizer("sequence",
				sequenceProperties.getTableName(), mongoClient);
		if (!sequenceProperties.isLazyInit()) {
			synchronizer.init();
		}
		return synchronizer;
	}

}
