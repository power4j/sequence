package com.power4j.kit.seq.persistent.provider;

import com.power4j.kit.seq.core.LongSeqPool;
import com.power4j.kit.seq.core.exceptions.SeqException;
import com.power4j.kit.seq.persistent.AddState;
import com.power4j.kit.seq.persistent.SeqSynchronizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 这个类用于测试
 *
 * @author CJ (power4j@outlook.com)
 * @date 2021/9/2
 * @since 1.0
 */
public class InMemorySeqSynchronizer implements SeqSynchronizer {

	private final boolean RE_ROLL = true;

	protected final AtomicLong queryCount = new AtomicLong();

	protected final AtomicLong updateCount = new AtomicLong();

	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

	private final Lock rLock = rwLock.readLock();

	private final Lock wLock = rwLock.writeLock();

	private final Map<String, LongSeqPool> map = new HashMap<>(8);

	@Override
	public boolean tryCreate(String name, String partition, long nextValue) {
		final String key = makeKey(name, partition);
		LongSeqPool pre;
		wLock.lock();
		try {
			pre = map.putIfAbsent(key, LongSeqPool.startFrom(key, 1L, RE_ROLL));
		}
		finally {
			wLock.unlock();
		}
		return Objects.isNull(pre);
	}

	@Override
	public boolean tryUpdate(String name, String partition, long nextValueOld, long nextValueNew) {
		final String key = makeKey(name, partition);
		wLock.lock();
		try {
			LongSeqPool seq = map.get(key);
			if (Objects.nonNull(seq)) {
				updateCount.incrementAndGet();
				if (nextValueOld == seq.peek()) {
					map.put(key, LongSeqPool.startFrom(key, nextValueNew, RE_ROLL));
					return true;
				}
			}
		}
		finally {
			wLock.unlock();
		}
		return false;
	}

	@Override
	public AddState tryAddAndGet(String name, String partition, int delta, int maxReTry) {
		final String key = makeKey(name, partition);
		LongSeqPool seqOld;
		wLock.lock();
		try {
			seqOld = map.get(key);
			if (Objects.nonNull(seqOld)) {
				queryCount.incrementAndGet();
				LongSeqPool seqNew = LongSeqPool.startFrom(key, seqOld.peek() + delta, RE_ROLL);
				map.put(key, seqNew);
				updateCount.incrementAndGet();
				return AddState.success(seqOld.peek(), seqNew.peek(), 1);
			}
			throw new SeqException(key + " not exists");
		}
		finally {
			wLock.unlock();
		}
	}

	@Override
	public Optional<Long> getNextValue(String name, String partition) {
		final String key = makeKey(name, partition);
		LongSeqPool seq;
		rLock.lock();
		try {
			seq = map.get(key);
		}
		finally {
			rLock.unlock();
		}
		if (Objects.nonNull(seq)) {
			queryCount.incrementAndGet();
		}
		return Optional.ofNullable(seq).map(LongSeqPool::next);
	}

	@Override
	public void init() {
		// Nothing
	}

	@Override
	public long getQueryCounter() {
		return SeqSynchronizer.super.getQueryCounter();
	}

	@Override
	public long getUpdateCounter() {
		return SeqSynchronizer.super.getUpdateCounter();
	}

	protected final String makeKey(String name, String partition) {
		return name + "/" + partition;
	}

}
