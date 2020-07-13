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
import com.power4j.kit.seq.persistent.provider.MySqlSynchronizer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 取号器，使用MysSql作为后端
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/4
 * @since 1.0
 */
@State(Scope.Benchmark)
@Threads(Threads.MAX)
@Fork(value = 1, jvmArgsAppend = { "-server", "-Xms32m", "-Xmx128m", "-Xmn64m", "-XX:CMSInitiatingOccupancyFraction=82",
		"-Xss256k", "-XX:LargePageSizeInBytes=64m" })
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 3, time = 10)
@OutputTimeUnit(TimeUnit.SECONDS)
public class MySqlSeqHolderBench {

	private static SeqSynchronizer synchronizer;

	private static SeqHolder seqHolder;

	@Setup
	public void setup() {
		String MYSQL_JDBC_URL = "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false";
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(MYSQL_JDBC_URL);
		config.setUsername("root");
		config.setPassword("root");
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "100");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		synchronizer = new MySqlSynchronizer("seq_bench", new HikariDataSource(config));
		synchronizer.init();
		seqHolder = new SeqHolder(synchronizer, "bench-test", LocalDateTime.now().toString(), BenchParam.SEQ_INIT_VAL,
				BenchParam.SEQ_POOL_SIZE, null);
		seqHolder.prepare();
	}

	@Benchmark
	public void longSeqPoolTest() {
		seqHolder.next();
	}

	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder().include(MySqlSeqHolderBench.class.getSimpleName()).build();
		new Runner(opt).run();
	}

}
