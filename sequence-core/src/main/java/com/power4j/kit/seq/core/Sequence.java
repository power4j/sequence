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

import com.power4j.kit.seq.core.exceptions.SeqException;

import java.util.Optional;

/**
 * 序号生成器
 *
 * @author CJ (power4j@outlook.com)
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
	 * @throws SeqException 无法获得序号抛出异常
	 */
	T next() throws SeqException;

	/**
	 * 取值
	 * @return 无法获得序号返回 null
	 */
	Optional<T> nextOpt();

	/**
	 * 返回经过格式化后字符串
	 * @return
	 * @throws SeqException 无法获得序号返回 null
	 */
	default Optional<String> nextStrOpt() {
		return nextOpt().map(v -> v.toString());
	}

	/**
	 * 返回经过格式化后字符串
	 * @return
	 * @throws SeqException 无法获得序号抛出异常
	 */
	default String nextStr() throws SeqException {
		return nextStrOpt().orElseThrow(() -> new SeqException("Nothing to offer"));
	}

}
