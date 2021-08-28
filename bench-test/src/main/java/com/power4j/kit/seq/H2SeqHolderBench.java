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
import com.power4j.kit.seq.persistent.provider.H2Synchronizer;
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
 * 取号器，使用H2作为后端
 *
 * @author lishangbu
 * @date 2021/8/28
 * @since 1.5.0
 */
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 3, time = 10)
@OutputTimeUnit(TimeUnit.SECONDS)
public class H2SeqHolderBench {

	private final static String DEFAULT_H2_JDBC_URL = "jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_DELAY=-1";

	private final static String JDBC_URL = EnvUtil.getStr("TEST_H2_URL", DEFAULT_H2_JDBC_URL);

	private static SeqSynchronizer synchronizer;

	private static SeqHolder seqHolder;

	@Setup
	public void setup() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(JDBC_URL);
		config.setUsername(EnvUtil.getStr("TEST_H2_USER", "sa"));
		config.setPassword(EnvUtil.getStr("TEST_H2_PWD", ""));
		synchronizer = new H2Synchronizer("seq_bench", new HikariDataSource(config));
		synchronizer.init();
		seqHolder = new SeqHolder(synchronizer, "h2-bench-test", TestUtil.getPartitionName(), BenchParam.SEQ_INIT_VAL,
				BenchParam.SEQ_POOL_SIZE, null);
		seqHolder.prepare();
	}

	@Benchmark
	@Threads(1)
	public void testSingleThread(Blackhole bh) {
		bh.consume(seqHolder.next());
	}

	@Benchmark
	@Threads(4)
	public void test4Threads(Blackhole bh) {
		bh.consume(seqHolder.next());
	}

	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder().include(H2SeqHolderBench.class.getSimpleName()).build();
		new Runner(opt).run();
	}

}
