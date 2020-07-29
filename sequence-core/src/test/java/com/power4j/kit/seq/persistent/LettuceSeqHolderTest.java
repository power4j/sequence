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

package com.power4j.kit.seq.persistent;

import com.power4j.kit.seq.TestUtil;
import com.power4j.kit.seq.core.SeqFormatter;
import com.power4j.kit.seq.persistent.provider.SimpleLettuceSynchronizer;
import com.power4j.kit.seq.persistent.provider.TestServices;
import io.lettuce.core.RedisClient;
import org.junit.After;
import org.junit.Before;

public class LettuceSeqHolderTest extends SeqHolderTestCase {

	public final static String SEQ_CACHE_NAME = "power4j:" + LettuceSeqHolderTest.class.getSimpleName();

	public final String seqName = "seq_holder_test";

	public final String partition = TestUtil.strNow();

	private RedisClient redisClient;

	private SimpleLettuceSynchronizer seqSynchronizer;

	private SeqHolder holder;

	@Before
	public void setUp() {
		redisClient = TestServices.getRedisClient();
		SimpleLettuceSynchronizer seqSynchronizer = new SimpleLettuceSynchronizer(SEQ_CACHE_NAME, redisClient);
		seqSynchronizer.init();
		holder = new SeqHolder(seqSynchronizer, seqName, partition, 1L, 1000, SeqFormatter.DEFAULT_FORMAT);
		holder.prepare();
	}

	@After
	public void tearDown() {
		if (redisClient != null) {
			redisClient.shutdown();
		}
	}

	@Override
	protected SeqHolder getSeqHolder() {
		return holder;
	}

}