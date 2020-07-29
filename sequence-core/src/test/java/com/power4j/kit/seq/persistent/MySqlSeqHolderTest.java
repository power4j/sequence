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
import com.power4j.kit.seq.persistent.provider.MySqlSynchronizer;
import com.power4j.kit.seq.persistent.provider.TestServices;
import org.junit.After;
import org.junit.Before;

public class MySqlSeqHolderTest extends SeqHolderTestCase {

	public final static String SEQ_TABLE = "tb_seq";

	public final String seqName = "power4j_" + MySqlSeqHolderTest.class.getSimpleName();

	public final String partition = TestUtil.strNow();

	private MySqlSynchronizer seqSynchronizer;

	private SeqHolder holder;

	@Before
	public void setUp() {
		seqSynchronizer = new MySqlSynchronizer(SEQ_TABLE, TestServices.getMySqlDataSource());
		seqSynchronizer.init();
		holder = new SeqHolder(seqSynchronizer, seqName, partition, 1L, 1000, SeqFormatter.DEFAULT_FORMAT);
		holder.prepare();
	}

	@After
	public void tearDown() {
		if (seqSynchronizer != null) {
			seqSynchronizer.dropTable();
		}
	}

	@Override
	protected SeqHolder getSeqHolder() {
		return holder;
	}

}