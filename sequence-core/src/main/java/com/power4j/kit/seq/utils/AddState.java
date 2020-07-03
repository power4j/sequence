package com.power4j.kit.seq.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 加法操作执行结果
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/2
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class AddState {

	/**
	 *
	 */
	private boolean success;

	/**
	 * 前一个值
	 */
	private Long previous;

	/**
	 * 当前值
	 */
	private Long current;

	/**
	 * 操作次数
	 */
	private int totalOps;

	public static AddState fail(int totalOps) {
		return new AddState(false, null, null, totalOps);
	}

	public static AddState success(long previous, long current, int totalOps) {
		return new AddState(true, previous, current, totalOps);
	}

}
