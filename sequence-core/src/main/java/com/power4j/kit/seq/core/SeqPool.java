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

package com.power4j.kit.seq.core;

/**
 * 序号池
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/6/30
 * @since 1.0
 */
public interface SeqPool<T, S extends SeqPool<T, S>> extends Sequence<T> {

	/**
	 * 查看当前计数器的值,注意此方法不能保证取值的有效性
	 * @return
	 */
	T peek();

	/**
	 * 是否还能取值
	 * @return
	 */
	boolean hasMore();

	/**
	 * 分离,得到一个新的{@code SeqPool},其起始值就是本实例的当前值
	 * @param name 新实例的名称
	 * @return
	 */
	S fork(String name);

	/**
	 * 号池中的剩余序号数量
	 * @return
	 */
	long remaining();

	/**
	 * 号池的容量（容量与用量无关）
	 * @return
	 */
	long capacity();

	/**
	 * 最小值
	 * @return
	 */
	T minValue();

	/**
	 * 最大值
	 * @return
	 */
	T maxValue();

}
