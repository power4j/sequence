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
import org.junit.After;
import org.junit.Before;

/**
 * H2 测试
 * <p>
 *
 * @author lishangbu
 * @date 2021/8/28
 * @since 1.5.0
 */
public class H2SynchronizerTest extends SynchronizerTestCase {

	public final static String SEQ_TABLE = "tb_seq";

	private H2Synchronizer h2Synchronizer;

	private H2Synchronizer createSeqSynchronizer() {
		H2Synchronizer sqlSynchronizer = new H2Synchronizer(SEQ_TABLE, TestServices.getH2DataSource());
		sqlSynchronizer.init();
		return sqlSynchronizer;
	}

	@Before
	public void setUp() {
		h2Synchronizer = createSeqSynchronizer();
	}

	@After
	public void tearDown() {
		if (h2Synchronizer != null) {
			h2Synchronizer.dropTable();
		}
	}

	@Override
	protected SeqSynchronizer getSeqSynchronizer() {
		return h2Synchronizer;
	}

}