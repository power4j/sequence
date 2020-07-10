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
