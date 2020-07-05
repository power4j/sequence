/*
 * Copyright (c) 2020, ChenJun(powe4j@outlook.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.power4j.kit.seq.core;

import com.power4j.kit.seq.core.exceptions.SeqException;

import java.util.Optional;
import java.util.function.Function;

/**
 * 序号池
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/6/30
 * @since 1.0
 */
public interface SeqPool<T, S extends SeqPool<T, S>> {

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

	/**
	 * 取值并转换
	 * @param converter
	 * @return
	 * @throws SeqException 号池耗尽抛出异常
	 */
	default <R> R next(Function<T, R> converter) throws SeqException {
		return converter.apply(next());
	}

}
