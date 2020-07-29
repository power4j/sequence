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
import com.power4j.kit.seq.persistent.provider.PostgreSqlSynchronizer;
import com.power4j.kit.seq.persistent.provider.TestServices;
import org.junit.After;
import org.junit.Before;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/29
 * @since 1.3
 */
public class PostgreSqlSeqHolderTest extends SeqHolderTestCase {

	public final static String SEQ_TABLE = "tb_seq";

	public final String seqName = "power4j_" + PostgreSqlSeqHolderTest.class.getSimpleName();

	public final String partition = TestUtil.strNow();

	private PostgreSqlSynchronizer seqSynchronizer;

	private SeqHolder holder;

	@Before
	public void setUp() {
		seqSynchronizer = new PostgreSqlSynchronizer(SEQ_TABLE, TestServices.getPostgreSqlDataSource());
		seqSynchronizer.init();
		holder = new SeqHolder(seqSynchronizer, seqName, partition, 1L, 1000, SeqFormatter.DEFAULT_FORMAT);
		holder.prepare();
	}

	@After
	public void tearDown() {
		seqSynchronizer.dropTable();
	}

	@Override
	protected SeqHolder getSeqHolder() {
		return holder;
	}

}
