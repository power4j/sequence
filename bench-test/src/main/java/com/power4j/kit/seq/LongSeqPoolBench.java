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

import com.power4j.kit.seq.core.LongSeqPool;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 单机序号池测试
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 3, time = 10)
@OutputTimeUnit(TimeUnit.SECONDS)
public class LongSeqPoolBench {

	private LongSeqPool longSeqPool;

	@Setup
	public void setup() {
		longSeqPool = LongSeqPool.forRange("longSeqPool", BenchParam.SEQ_INIT_VAL, Long.MAX_VALUE, false);
	}

	@Benchmark
	@Threads(1)
	public void testSingleThread(Blackhole bh) {
		bh.consume(longSeqPool.next());
	}

	@Benchmark
	@Threads(4)
	public void test4Threads(Blackhole bh) {
		bh.consume(longSeqPool.next());
	}

	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder().include(LongSeqPoolBench.class.getSimpleName()).build();
		new Runner(opt).run();
	}

}
