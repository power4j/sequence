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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * 预置动态分区方法
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/5
 * @since 1.0
 */
public interface Partitions {

	/**
	 * 按年份分区
	 */
	Supplier<String> ANNUALLY = () -> LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));

	/**
	 * 按月份分区
	 */
	Supplier<String> MONTHLY = () -> LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

	/**
	 * 按日期分区
	 */
	Supplier<String> DAILY = () -> LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

}
