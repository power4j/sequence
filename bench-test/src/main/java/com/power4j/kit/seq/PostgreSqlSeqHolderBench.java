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

package com.power4j.kit.seq;

import com.power4j.kit.seq.persistent.SeqHolder;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import com.power4j.kit.seq.persistent.provider.PostgreSqlSynchronizer;
import com.power4j.kit.seq.utils.EnvUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/29
 * @since 1.3
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 3, time = 10)
@OutputTimeUnit(TimeUnit.SECONDS)
public class PostgreSqlSeqHolderBench {

	private final static String DEFAULT_POSTGRESQL_JDBC_URL = "jdbc:postgresql://127.0.0.1:5432/test?ssl=false";

	private final static String JDBC_URL = EnvUtil.getStr("TEST_POSTGRESQL_URL", DEFAULT_POSTGRESQL_JDBC_URL);

	private static SeqSynchronizer synchronizer;

	private static SeqHolder seqHolder;

	@Setup
	public void setup() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(JDBC_URL);
		config.setUsername(EnvUtil.getStr("TEST_POSTGRESQL_USER", "root"));
		config.setPassword(EnvUtil.getStr("TEST_POSTGRESQL_PWD", "root"));
		synchronizer = new PostgreSqlSynchronizer("seq_bench", new HikariDataSource(config));
		synchronizer.init();
		seqHolder = new SeqHolder(synchronizer, "postgresql-bench-test", TestUtil.getPartitionName(),
				BenchParam.SEQ_INIT_VAL, BenchParam.SEQ_POOL_SIZE, null);
		seqHolder.prepare();
	}

	@Benchmark
	@Threads(1)
	public void testSingleThread(Blackhole bh) {
		bh.consume(seqHolder.next());
	}

	@Benchmark
	@Threads(4)
	public void test4Thread(Blackhole bh) {
		bh.consume(seqHolder.next());
	}

	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder().include(PostgreSqlSeqHolderBench.class.getSimpleName()).build();
		new Runner(opt).run();
	}

}
