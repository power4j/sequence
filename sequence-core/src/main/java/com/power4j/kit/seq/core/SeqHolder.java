package com.power4j.kit.seq.core;

import com.power4j.kit.seq.persistent.SeqSynchronizer;
import com.power4j.kit.seq.utils.AddState;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 序号领用器,负责从外部领用序号,缓存到本地。
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
public class SeqHolder {

	private final SeqSynchronizer seqSynchronizer;

	private final String name;

	private final Supplier<String> partitionFunc;

	private final long initValue;

	private final int poolSize;

	private final SeqFormatter seqFormatter;

	private final AtomicLong pollCount = new AtomicLong();

	private final AtomicReference<LongSeqPool> poolRef = new AtomicReference<>();

	private final AtomicReference<String> currentPartitionRef = new AtomicReference<>();

	/**
	 * 构造方法
	 * @param seqSynchronizer 同步器
	 * @param name 名称
	 * @param partitionFunc 窗口函数
	 * @param initValue 初始值,号池不存在时使用
	 * @param poolSize 表示单次申请序号数量
	 * @param seqFormatter 自定义格式化输出
	 */
	public SeqHolder(SeqSynchronizer seqSynchronizer, String name, Supplier<String> partitionFunc, long initValue,
			int poolSize, SeqFormatter seqFormatter) {
		this.seqSynchronizer = seqSynchronizer;
		this.name = name;
		this.partitionFunc = partitionFunc;
		this.initValue = initValue;
		this.poolSize = poolSize;
		this.seqFormatter = seqFormatter;
	}

	/**
	 * 构造方法
	 * @param seqSynchronizer 同步器
	 * @param name 名称
	 * @param partitionFunc 窗口函数
	 * @param initValue 初始值,号池不存在时使用
	 * @param poolSize 表示单次申请序号数量
	 */
	public SeqHolder(SeqSynchronizer seqSynchronizer, String name, Supplier<String> partitionFunc, long initValue,
			int poolSize) {
		this(seqSynchronizer, name, partitionFunc, initValue, poolSize, SeqFormatter.DEFAULT);
	}

	/**
	 * 取值
	 * @return
	 */
	public Optional<Long> next() {
		return poolRef.updateAndGet(o -> {
			if (o == null || !o.hasMore()) {
				pollCount.incrementAndGet();
				return pull();
			}
			return o;
		}).nextOpt();
	}

	/**
	 * 取值并且格式化
	 * @return
	 */
	public Optional<String> nextFormatted() {
		SeqFormatter formatter = seqFormatter == null ? SeqFormatter.DEFAULT : seqFormatter;
		return next().map(n -> formatter.format(name, currentPartitionRef.get(), n));
	}

	private LongSeqPool pull() {
		String partition = partitionFunc.get();
		if (seqSynchronizer.tryCreate(name, partition, initValue + poolSize)) {
			currentPartitionRef.set(partition);
			return LongSeqPool.forSize(makePoolName(name, partition), initValue, poolSize, false);
		}
		else {
			AddState state = seqSynchronizer.tryAddAndGet(name, partition, poolSize, -1);
			return LongSeqPool.forRange(makePoolName(name, partition), state.getPrevious(), state.getCurrent(), false);
		}
	}

	public long getPullCount() {
		return pollCount.get();
	}

	protected String makePoolName(String seqName, String window) {
		return seqName + "/" + window;
	}

}
