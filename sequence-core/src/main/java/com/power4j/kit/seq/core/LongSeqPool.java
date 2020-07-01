package com.power4j.kit.seq.core;

import com.power4j.kit.seq.core.exceptions.SeqException;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 序号池(Long型)
 * <ul>
 * <li>取值范围{@code [Long.MIN_VALUE,Long.MAX_VALUE - 1]}</li>
 * <li>支持一次性取号和循环取号</li>
 * <li>序号可以是负数</li>
 * <li>线程安全,lock free</li>
 * </ul>
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/6/30
 * @since 1.0
 */
public class LongSeqPool implements SeqPool<Long, LongSeqPool> {

	private final static long ZERO = 0L;

	private final static long ONE = 1L;

	public final static long MAX_VALUE = Long.MAX_VALUE - ONE;

	/**
	 * 表示一个号池外的值
	 */
	public final static long OUT_OF_POOL = Long.MAX_VALUE;

	@Getter
	private final long start;

	@Getter
	private final long end;

	@Getter
	private final boolean reRoll;

	private final AtomicLong current;

	/**
	 * 根据数量创建
	 * @param start 起始值，包含
	 * @param size 数量,以步长为单位
	 * @param reRoll 是否允许滚动
	 * @return
	 */
	public static LongSeqPool forSize(long start, int size, boolean reRoll) {
		return new LongSeqPool(start, start + size, reRoll);
	}

	/**
	 * 根据区间创建
	 * @param min 起始值，包含
	 * @param max end 结束值，包含，最大值为 {@code Long.MAX_VALUE - 1 }
	 * @param reRoll 是否允许滚动
	 * @return
	 */
	public static LongSeqPool forRange(long min, long max, boolean reRoll) {
		if (max >= MAX_VALUE) {
			throw new IllegalArgumentException("invalid max value");
		}
		return new LongSeqPool(min, max + ONE, reRoll);
	}

	/**
	 * constructor
	 * @param start 起始值，包含
	 * @param end 结束值，不包含
	 * @param reRoll 是否允许滚动，可以滚动的号池永远不会耗尽
	 */
	private LongSeqPool(long start, long end, boolean reRoll) {
		this.start = start;
		this.end = end;
		this.reRoll = reRoll;
		this.current = new AtomicLong(start);
		if (end - start < ONE) {
			throw new IllegalArgumentException("nothing to offer");
		}
	}

	@Override
	public Long next() throws SeqException {
		return take();
	}

	@Override
	public Optional<Long> nextOpt() {
		return Optional.ofNullable(hasMore() ? take() : null);
	}

	@Override
	public Long peek() {
		return current.get();
	}

	/**
	 * 取出序号
	 * @return
	 * @throws SeqException 号池耗尽抛出异常，可以通过 {@code hasMore} 方法提前检查
	 * @see SeqPool#hasMore
	 */
	public long take() {
		final long val = take(OUT_OF_POOL);
		if (!reRoll && val == OUT_OF_POOL) {
			throw new SeqException("no more value");
		}
		return val;
	}

	/**
	 * 取出序号，号池耗尽返回默认值
	 * @param defVal 号池耗尽时返回的默认值，必须是号池范围外的值,推荐使用 {@code OUT_OF_POOL}
	 * @return
	 * @throws IllegalArgumentException defVal 无效
	 * @see LongSeqPool#OUT_OF_POOL
	 */
	public long take(long defVal) {
		if (defVal >= start && defVal < end) {
			throw new IllegalArgumentException("bad defVal");
		}
		return current.getAndUpdate(n -> updateFunc(n, defVal));
	}

	@Override
	public boolean hasMore() {
		return remaining() > ZERO;
	}

	@Override
	public LongSeqPool fork() {
		LongSeqPool seqPool = new LongSeqPool(start, end, reRoll);
		seqPool.setCurrent(peek());
		return seqPool;
	}

	@Override
	public long remaining() {
		if (reRoll) {
			return capacity();
		}
		else {
			final long val = peek();
			return val > end ? ZERO : end - val;
		}
	}

	@Override
	public long capacity() {
		return end - start;
	}

	@Override
	public Long minValue() {
		return start;
	}

	@Override
	public Long maxValue() {
		return end - ONE;
	}

	private long updateFunc(final long pre, long defVal) {
		if (pre >= maxValue()) {
			return reRoll ? minValue() : defVal;
		}
		return pre + ONE;
	}

	private void setCurrent(long val) {
		current.set(val);
	}

	@Override
	public String toString() {
		return peek() + " -> [" + start + "," + end + "),reRoll = " + reRoll;
	}

}
