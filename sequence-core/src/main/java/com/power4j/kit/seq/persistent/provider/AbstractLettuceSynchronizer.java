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
import com.power4j.kit.seq.persistent.AddState;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.api.sync.RedisScriptingCommands;
import io.lettuce.core.api.sync.RedisStringCommands;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/10
 * @since 1.1
 */
@Slf4j
public abstract class AbstractLettuceSynchronizer implements SeqSynchronizer {

	private final AtomicBoolean checkKey = new AtomicBoolean(true);

	private final AtomicLong queryCounter = new AtomicLong();

	private final AtomicLong updateCounter = new AtomicLong();

	private final AtomicReference<String> serverScript = new AtomicReference<>();

	private final String cacheName;

	public AbstractLettuceSynchronizer(String cacheName) {
		this.cacheName = cacheName;
	}

	protected String makeKey(String seqName, String partition) {
		return cacheName + RedisConstants.KEY_DELIMITER + seqName + RedisConstants.KEY_DELIMITER + partition;
	}

	protected void validateKeyExists(RedisStringCommands<String, String> redisCommands, String key, String msg) {
		if (null == redisCommands.get(key)) {
			throw new SeqException(msg);
		}
	}

	protected String loadScript(RedisScriptingCommands<String, String> redisCommands, String script) {
		String id = redisCommands.scriptLoad(script);
		log.info("Script loaded,id = {}", id);
		return id;
	}

	protected boolean doUpdate(RedisScriptingCommands<String, String> redisCommands, String name, String partition,
			long nextValueOld, long nextValueNew) {
		String scriptId = serverScript
				.updateAndGet((s -> s != null ? s : loadScript(redisCommands, RedisConstants.UPDATE_SCRIPT)));
		String[] keys = { makeKey(name, partition) };
		boolean ret = redisCommands.evalsha(scriptId, ScriptOutputType.BOOLEAN, keys, Long.toString(nextValueOld),
				Long.toString(nextValueNew));
		updateCounter.incrementAndGet();
		return ret;
	}

	protected Optional<Long> doGet(RedisStringCommands<String, String> redisCommands, String name, String partition) {
		String val = redisCommands.get(makeKey(name, partition));
		queryCounter.incrementAndGet();
		return Optional.ofNullable(Long.parseLong(val));
	}

	protected AddState doInc(RedisStringCommands<String, String> redisCommands, String name, String partition,
			int delta) {
		final String key = makeKey(name, partition);
		if (checkKey.get()) {
			validateKeyExists(redisCommands, key, "Key not exists:" + key);
		}
		long current = redisCommands.incrby(key, delta);
		updateCounter.incrementAndGet();
		return AddState.success(current - delta, current, 1);
	}

	public int removeCache() {
		return execKeyCommand((cmd -> {
			int keys = 0;
			ScanCursor scanCursor = ScanCursor.INITIAL;
			ScanArgs scanArgs = ScanArgs.Builder.limit(10).match(cacheName + RedisConstants.KEY_DELIMITER + "*");
			while (true) {
				KeyScanCursor<String> keyScanCursor = cmd.scan(scanCursor, scanArgs);
				if (keyScanCursor.isFinished() || keyScanCursor.getKeys().size() <= 0) {
					break;
				}
				keys += keyScanCursor.getKeys().size();
				cmd.del(keyScanCursor.getKeys().toArray(new String[keyScanCursor.getKeys().size()]));
				scanCursor = ScanCursor.of(keyScanCursor.getCursor());
			}
			return keys;
		}));
	}

	public boolean setKeyValidate(boolean check) {
		return checkKey.getAndSet(check);
	}

	/**
	 * 执行命令
	 * @param func
	 * @param <R>
	 * @return
	 */
	protected abstract <R> R execStringCommand(Function<RedisStringCommands<String, String>, R> func);

	/**
	 * 执行命令
	 * @param func
	 * @param <R>
	 * @return
	 */
	protected abstract <R> R execScriptingCommand(Function<RedisScriptingCommands<String, String>, R> func);

	/**
	 * 执行命令
	 * @param func
	 * @param <R>
	 * @return
	 */
	protected abstract <R> R execKeyCommand(Function<RedisKeyCommands<String, String>, R> func);

	@Override
	public boolean tryCreate(String name, String partition, long nextValue) {
		return execStringCommand((cmd) -> cmd.setnx(makeKey(name, partition), Long.toString(nextValue)));
	}

	@Override
	public boolean tryUpdate(String name, String partition, long nextValueOld, long nextValueNew) {
		return execScriptingCommand((cmd) -> doUpdate(cmd, name, partition, nextValueOld, nextValueNew));
	}

	@Override
	public AddState tryAddAndGet(String name, String partition, int delta, int maxReTry) {
		return execStringCommand((cmd) -> doInc(cmd, name, partition, delta));
	}

	@Override
	public Optional<Long> getNextValue(String name, String partition) {
		return execStringCommand((cmd) -> doGet(cmd, name, partition));
	}

	@Override
	public void init() {
		execScriptingCommand((cmd) -> loadScript(cmd, RedisConstants.UPDATE_SCRIPT));
	}

	@Override
	public long getQueryCounter() {
		return queryCounter.get();
	}

	@Override
	public long getUpdateCounter() {
		return updateCounter.get();
	}

}
