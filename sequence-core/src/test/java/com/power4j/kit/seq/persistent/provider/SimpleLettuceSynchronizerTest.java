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

import com.power4j.kit.seq.persistent.SeqSynchronizer;
import io.lettuce.core.RedisClient;
import org.junit.After;

public class SimpleLettuceSynchronizerTest extends SynchronizerTestCase {

	public final static String SEQ_CACHE_NAME = "power4j:seq-test";

	private RedisClient redisClient;

	private SimpleLettuceSynchronizer createSeqSynchronizer() {

		redisClient = TestServices.getRedisClient();
		SimpleLettuceSynchronizer simpleLettuceSynchronizer = new SimpleLettuceSynchronizer(SEQ_CACHE_NAME,
				redisClient);
		simpleLettuceSynchronizer.init();
		return simpleLettuceSynchronizer;
	}

	@After
	public void tearDown() {
		if (redisClient != null) {
			redisClient.shutdown();
		}
	}

	@Override
	protected SeqSynchronizer getSeqSynchronizer() {
		return createSeqSynchronizer();
	}

}