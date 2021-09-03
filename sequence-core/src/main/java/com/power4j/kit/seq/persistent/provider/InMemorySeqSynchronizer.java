package com.power4j.kit.seq.persistent.provider;

import com.power4j.kit.seq.core.exceptions.SeqException;
import com.power4j.kit.seq.persistent.AddState;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import lombok.Getter;
import lombok.Setter;

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

	protected final AtomicLong queryCount = new AtomicLong();

	protected final AtomicLong updateCount = new AtomicLong();

	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

	private final Lock rLock = rwLock.readLock();

	private final Lock wLock = rwLock.writeLock();

	private final Map<String, Row> map = new HashMap<>(8);

	@Override
	public boolean tryCreate(String name, String partition, long nextValue) {
		final String key = makeKey(name, partition);
		Row pre;
		wLock.lock();
		try {
			pre = map.putIfAbsent(key, Row.of(key, nextValue));
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
			Row pre = map.get(key);
			if (Objects.nonNull(pre)) {
				updateCount.incrementAndGet();
				if (nextValueOld == pre.getNextValue()) {
					map.put(key, Row.of(key, nextValueNew));
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
		Row pre;
		wLock.lock();
		try {
			pre = map.get(key);
			if (Objects.nonNull(pre)) {
				queryCount.incrementAndGet();
				Row row = Row.of(key, pre.getNextValue() + delta);
				map.put(key, row);
				updateCount.incrementAndGet();
				return AddState.success(pre.getNextValue(), row.getNextValue(), 1);
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
		Row row;
		rLock.lock();
		try {
			row = map.get(key);
		}
		finally {
			rLock.unlock();
		}
		if (Objects.nonNull(row)) {
			queryCount.incrementAndGet();
		}
		return Optional.ofNullable(row).map(Row::getNextValue);
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

	@Getter
	@Setter
	static class Row {

		private final String id;

		private Long nextValue;

		public static Row of(String id, Long nextValue) {
			return new Row(id, nextValue);
		}

		Row(String id, Long nextValue) {
			this.id = id;
			this.nextValue = nextValue;
		}

	}

}
