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

import com.power4j.kit.seq.persistent.SeqSynchronizer;
import com.power4j.kit.seq.persistent.provider.LettuceClusterSynchronizer;
import com.power4j.kit.seq.persistent.provider.SimpleLettuceSynchronizer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/13
 * @since 1.0
 */
@Configuration
public class RedisSynchronizerConfigure {

	@Bean(destroyMethod = "shutdown")
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = SequenceProperties.PREFIX, name = "backend", havingValue = "redis")
	public RedisClient redisClient(SequenceProperties sequenceProperties) {
		RedisURI redisUri = RedisURI.create(sequenceProperties.getLettuceUri());
		return RedisClient.create(redisUri);
	}

	@Bean(destroyMethod = "shutdown")
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = SequenceProperties.PREFIX, name = "backend", havingValue = "redis-cluster")
	public RedisClusterClient redisClusterClient(SequenceProperties sequenceProperties) {
		RedisURI redisUri = RedisURI.create(sequenceProperties.getLettuceUri());
		return RedisClusterClient.create(redisUri);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(RedisClient.class)
	public SeqSynchronizer redisSynchronizer(SequenceProperties sequenceProperties, RedisClient redisClient) {
		SimpleLettuceSynchronizer synchronizer = new SimpleLettuceSynchronizer(sequenceProperties.getTableName(),
				redisClient);
		if (!sequenceProperties.isLazyInit()) {
			synchronizer.init();
		}
		return synchronizer;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(RedisClusterClient.class)
	public SeqSynchronizer redisClusterSynchronizer(SequenceProperties sequenceProperties,
			RedisClusterClient redisClusterClient) {
		LettuceClusterSynchronizer synchronizer = new LettuceClusterSynchronizer(sequenceProperties.getTableName(),
				redisClusterClient);
		if (!sequenceProperties.isLazyInit()) {
			synchronizer.init();
		}
		return synchronizer;
	}

}
