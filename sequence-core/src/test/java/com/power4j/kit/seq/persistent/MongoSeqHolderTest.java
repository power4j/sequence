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

import com.mongodb.client.MongoClient;
import com.power4j.kit.seq.TestUtil;
import com.power4j.kit.seq.core.SeqFormatter;
import com.power4j.kit.seq.persistent.provider.SimpleMongoSynchronizer;
import com.power4j.kit.seq.persistent.provider.TestServices;
import org.junit.After;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/20
 * @since 1.0
 */
public class MongoSeqHolderTest extends SeqHolderTestCase {

	public final static String DB_NAME = "seq_test";

	public final static String COL_NAME = "power4j_" + MongoSeqHolderTest.class.getSimpleName();

	public final String seqName = "holder_test";

	public final String partition = TestUtil.strNow();

	private MongoClient mongoClient;

	public SeqHolder createSeqHolder() {
		mongoClient = TestServices.getMongoClient();
		SimpleMongoSynchronizer seqSynchronizer = new SimpleMongoSynchronizer(DB_NAME, COL_NAME, mongoClient);
		seqSynchronizer.init();
		SeqHolder holder = new SeqHolder(seqSynchronizer, seqName, partition, 1L, 1000, SeqFormatter.DEFAULT_FORMAT);
		holder.prepare();
		return holder;
	}

	@After
	public void tearDown() {
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	@Override
	protected SeqHolder getSeqHolder() {
		return createSeqHolder();
	}

}
