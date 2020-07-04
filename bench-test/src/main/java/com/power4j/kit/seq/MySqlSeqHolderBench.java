package com.power4j.kit.seq;

import com.power4j.kit.seq.persistent.SeqHolder;
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
 * @author CJ (jclazz@outlook.com)
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

	private SeqHolder seqHolder;

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
		MySqlSynchronizer synchronizer = new MySqlSynchronizer("seq_bench", new HikariDataSource(config));
		synchronizer.createTable();
		seqHolder = new SeqHolder(synchronizer, "bench-test", LocalDateTime.now().toString(), BenchParam.SEQ_INIT_VAL,
				BenchParam.SEQ_POOL_SIZE, null);
	}

	@Benchmark
	public void longSeqPoolTest() {
		seqHolder.next().get();
	}

	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder().include(MySqlSeqHolderBench.class.getSimpleName()).build();
		new Runner(opt).run();
	}

}
