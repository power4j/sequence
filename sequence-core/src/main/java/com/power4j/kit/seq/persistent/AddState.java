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

package com.power4j.kit.seq.persistent;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 加法操作执行结果
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/2
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class AddState {

	/**
	 * 执行结果
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
