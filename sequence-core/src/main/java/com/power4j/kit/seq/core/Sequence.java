package com.power4j.kit.seq.core;

import com.power4j.kit.seq.core.exceptions.SeqException;

import java.util.Optional;

/**
 * 序号生成器
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/6
 * @since 1.0
 */
public interface Sequence<T> {

	/**
	 * 名称
	 * @return
	 */
	String getName();

	/**
	 * 取值
	 * @return
	 * @throws SeqException 号池耗尽抛出异常
	 */
	T next() throws SeqException;

	/**
	 * 取值
	 * @return
	 */
	Optional<T> nextOpt();

	/**
	 * 返回经过格式化后字符串
	 * @return
	 * @throws SeqException 号池耗尽抛出异常
	 */
	default Optional<String> nextStrOpt() throws SeqException {
		return nextOpt().map(v -> v.toString());
	}

	/**
	 * 返回经过格式化后字符串
	 * @return
	 * @throws SeqException 号池耗尽抛出异常
	 */
	default String nextStr() throws SeqException {
		return nextStrOpt().orElseThrow(() -> new SeqException("Nothing to offer"));
	}

}
