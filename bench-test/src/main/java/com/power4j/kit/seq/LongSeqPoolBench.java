/*
 * Copyright (c) 2020 ChenJun(power4j@outlook.com)
 * Sequence is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.power4j.kit.seq;

import com.power4j.kit.seq.core.LongSeqPool;
import org.openjdk.jmh.annotations.*;
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
@State(Scope.Benchmark)
@Threads(Threads.MAX)
@Fork(value = 1, jvmArgsAppend = { "-server", "-Xms32m", "-Xmx128m", "-Xmn64m", "-XX:CMSInitiatingOccupancyFraction=82",
		"-Xss256k", "-XX:LargePageSizeInBytes=64m" })
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 3, time = 10)
@OutputTimeUnit(TimeUnit.SECONDS)
public class LongSeqPoolBench {

	private LongSeqPool longSeqPool;

	@Setup
	public void setup() {
		longSeqPool = LongSeqPool.forSize("longSeqPool", BenchParam.SEQ_INIT_VAL, BenchParam.SEQ_POOL_SIZE, true);
	}

	@Benchmark
	public void longSeqPoolTest() {
		longSeqPool.next();
	}

	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder().include(LongSeqPoolBench.class.getSimpleName()).build();
		new Runner(opt).run();
	}

}
