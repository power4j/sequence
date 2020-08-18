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

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.power4j.kit.seq.persistent.SeqHolder;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import com.power4j.kit.seq.persistent.provider.SimpleMongoSynchronizer;
import com.power4j.kit.seq.utils.EnvUtil;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/20
 * @since 1.0
 */
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 3, time = 10)
@OutputTimeUnit(TimeUnit.SECONDS)
public class MongoSeqHolderBench {

	/**
	 * mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database.collection][?options]]
	 */
	private final static String MONGO_URI = EnvUtil.getStr("TEST_MONGO_URI", "mongodb://127.0.0.1:27017");

	private static SeqSynchronizer synchronizer;

	private static SeqHolder seqHolder;

	@Setup
	public void setup() {
		MongoClient mongoClient = MongoClients.create(MONGO_URI);
		synchronizer = new SimpleMongoSynchronizer("seq_bench", "seq_col", mongoClient);
		synchronizer.init();
		seqHolder = new SeqHolder(synchronizer, "mongo-bench-test", TestUtil.getPartitionName(),
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
	public void test4Threads(Blackhole bh) {
		bh.consume(seqHolder.next());
	}

	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder().include(MongoSeqHolderBench.class.getSimpleName()).build();
		new Runner(opt).run();
	}

}
