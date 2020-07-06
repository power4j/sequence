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

package com.power4j.kit.seq.persistent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * 预置动态分区方法
 *
 * @author CJ (jclazz@outlook.com)
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
