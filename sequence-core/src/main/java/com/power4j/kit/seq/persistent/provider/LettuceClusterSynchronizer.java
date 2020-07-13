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

package com.power4j.kit.seq.persistent.provider;

import com.power4j.kit.seq.core.exceptions.SeqException;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.api.sync.RedisScriptingCommands;
import io.lettuce.core.api.sync.RedisStringCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.function.Function;

/**
 * Lettuce 集群支持
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/12
 * @since 1.1
 */
public class LettuceClusterSynchronizer extends AbstractLettuceSynchronizer implements SeqSynchronizer {

	private final GenericObjectPool<StatefulRedisClusterConnection<String, String>> pool;

	public LettuceClusterSynchronizer(String cacheName, RedisClusterClient redisClient) {
		this(cacheName, redisClient, new GenericObjectPoolConfig());
	}

	public LettuceClusterSynchronizer(String cacheName, RedisClusterClient redisClient,
			GenericObjectPoolConfig<StatefulRedisClusterConnection<String, String>> poolConfig) {
		super(cacheName);
		this.pool = ConnectionPoolSupport.createGenericObjectPool(() -> redisClient.connect(), poolConfig);
	}

	public LettuceClusterSynchronizer(String cacheName,
			GenericObjectPool<StatefulRedisClusterConnection<String, String>> pool) {
		super(cacheName);
		this.pool = pool;
	}

	public <R> R execCommand(Function<RedisAdvancedClusterCommands<String, String>, R> func) {
		try (StatefulRedisClusterConnection<String, String> connection = getRedisConnection()) {
			return func.apply(connection.sync());
		}
	}

	protected StatefulRedisClusterConnection<String, String> getRedisConnection() {
		try {
			return pool.borrowObject();
		}
		catch (Exception e) {
			throw new SeqException(e.getMessage(), e);
		}
	}

	@Override
	public void shutdown() {
		pool.close();
	}

	@Override
	protected <R> R execStringCommand(Function<RedisStringCommands<String, String>, R> func) {
		try (StatefulRedisClusterConnection<String, String> connection = getRedisConnection()) {
			return func.apply(connection.sync());
		}
	}

	@Override
	protected <R> R execScriptingCommand(Function<RedisScriptingCommands<String, String>, R> func) {
		try (StatefulRedisClusterConnection<String, String> connection = getRedisConnection()) {
			return func.apply(connection.sync());
		}
	}

	@Override
	protected <R> R execKeyCommand(Function<RedisKeyCommands<String, String>, R> func) {
		try (StatefulRedisClusterConnection<String, String> connection = getRedisConnection()) {
			return func.apply(connection.sync());
		}
	}

}
