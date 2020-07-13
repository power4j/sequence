/*
 * Copyright 2020 ChenJun (power4j@outlook.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
