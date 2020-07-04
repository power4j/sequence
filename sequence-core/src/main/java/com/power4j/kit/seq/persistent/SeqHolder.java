package com.power4j.kit.seq.persistent;

import com.power4j.kit.seq.core.LongSeqPool;
import com.power4j.kit.seq.core.SeqFormatter;
import com.power4j.kit.seq.utils.AddState;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * 取号器
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
public class SeqHolder {

	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

	private final Lock rLock = rwLock.readLock();

	private final Lock wLock = rwLock.writeLock();

	private final SeqSynchronizer seqSynchronizer;

	private final String name;

	private final Supplier<String> partitionFunc;

	private final long initValue;

	private final int poolSize;

	private final SeqFormatter seqFormatter;

	private final AtomicLong pollCount = new AtomicLong();

	private final AtomicReference<String> currentPartitionRef = new AtomicReference<>();

	private volatile LongSeqPool seqPool;

	/**
	 * 构造方法
	 * @param seqSynchronizer 同步器
	 * @param name 名称
	 * @param partitionFunc 窗口函数
	 * @param initValue 初始值,号池不存在时使用
	 * @param poolSize 表示单次申请序号数量
	 * @param seqFormatter 自定义格式化输出，可选
	 */
	public SeqHolder(SeqSynchronizer seqSynchronizer, String name, Supplier<String> partitionFunc, long initValue,
			int poolSize, SeqFormatter seqFormatter) {
		this.seqSynchronizer = seqSynchronizer;
		this.name = name;
		this.partitionFunc = partitionFunc;
		this.initValue = initValue;
		this.poolSize = poolSize;
		this.seqFormatter = seqFormatter == null ? SeqFormatter.DEFAULT : seqFormatter;
		seqPool = fetch();
	}

	/**
	 * 构造方法
	 * @param seqSynchronizer 同步器
	 * @param name 名称
	 * @param partition 分区名称
	 * @param initValue 初始值,号池不存在时使用
	 * @param poolSize 表示单次申请序号数量
	 * @param seqFormatter 自定义格式化输出，可选
	 */
	public SeqHolder(SeqSynchronizer seqSynchronizer, String name, String partition, long initValue, int poolSize,
			SeqFormatter seqFormatter) {
		this(seqSynchronizer, name, () -> partition, initValue, poolSize, seqFormatter);
	}

	/**
	 * 取值
	 * @return
	 */
	public Optional<Long> next() {
		Optional<Long> val;
		rLock.lock();
		try {
			val = seqPool == null ? Optional.empty() : seqPool.nextOpt();
			if (val.isPresent()) {
				return val;
			}
		}
		finally {
			rLock.unlock();
		}
		return pull();
	}

	private final Optional<Long> pull() {
		Optional<Long> val;
		wLock.lock();
		try {
			val = (seqPool == null ? fetch() : seqPool).nextOpt();
			if (!val.isPresent()) {
				val = (seqPool = fetch()).nextOpt();
				if (!val.isPresent()) {
					throw new IllegalStateException("Bug detected : " + seqPool.toString());
				}
			}
			return val;
		}
		finally {
			wLock.unlock();
		}
	}

	/**
	 * 取值并且格式化
	 * @return
	 */
	public Optional<String> nextFormatted() {
		return next().map(n -> seqFormatter.format(name, currentPartitionRef.get(), n));
	}

	private boolean noMore(LongSeqPool seqPool) {
		return seqPool == null || !seqPool.hasMore();
	}

	private LongSeqPool fetch() {
		pollCount.incrementAndGet();
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
