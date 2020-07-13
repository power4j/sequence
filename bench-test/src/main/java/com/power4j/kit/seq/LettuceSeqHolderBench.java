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
import com.power4j.kit.seq.persistent.provider.SimpleLettuceSynchronizer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Lettuce 后端性能测试
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/12
 * @since 1.1
 */
@State(Scope.Benchmark)
@Threads(Threads.MAX)
@Fork(value = 1, jvmArgsAppend = { "-server", "-Xms32m", "-Xmx128m", "-Xmn64m", "-XX:CMSInitiatingOccupancyFraction=82",
		"-Xss256k", "-XX:LargePageSizeInBytes=64m" })
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 3, time = 10)
@OutputTimeUnit(TimeUnit.SECONDS)
public class LettuceSeqHolderBench {

	private static SeqSynchronizer synchronizer;

	private static SeqHolder seqHolder;

	@Setup
	public void setup() {
		final String partition = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		RedisURI redisUri = RedisURI.builder().withHost("127.0.0.1").withPort(6379).build();
		RedisClient redisClient = RedisClient.create(redisUri);

		synchronizer = new SimpleLettuceSynchronizer("lettuce_ben_test", redisClient);
		synchronizer.init();
		seqHolder = new SeqHolder(synchronizer, "lettuce_ben_test", partition, BenchParam.SEQ_INIT_VAL,
				BenchParam.SEQ_POOL_SIZE, null);
		seqHolder.prepare();
	}

	@Benchmark
	public void longSeqPoolTest() {
		seqHolder.next();
	}

	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder().include(LettuceSeqHolderBench.class.getSimpleName()).build();
		new Runner(opt).run();
	}

}
