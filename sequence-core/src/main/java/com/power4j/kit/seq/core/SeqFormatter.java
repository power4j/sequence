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
 * 格式化函数
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
@FunctionalInterface
public interface SeqFormatter {

	SeqFormatter DEFAULT_FORMAT = (seqName, partition, value) -> String.format("%s%08d", partition, value);

	SeqFormatter ANNUALLY_FORMAT = (seqName, partition, value) -> String.format("%s%10d", partition, value);

	SeqFormatter MONTHLY_FORMAT = (seqName, partition, value) -> String.format("%s%08d", partition, value);

	SeqFormatter DAILY_FORMAT = (seqName, partition, value) -> String.format("%s%06d", partition, value);

	/**
	 * 格式化
	 * @param seqName
	 * @param partition
	 * @param value
	 * @return
	 */
	String format(String seqName, String partition, long value);

}
