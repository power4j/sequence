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

package com.power4j.kit.seq.spring.boot.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/6
 * @since 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = SequenceProperties.PREFIX)
public class SequenceProperties {

	public final static String PREFIX = "power4j.sequence";

	/**
	 * 开关
	 */
	private boolean enabled = true;

	/**
	 * 后端类型
	 */
	private BackendTypeEnum backend;

	/**
	 * 懒加载
	 */
	private boolean lazyInit = false;

	/**
	 * 表名称(对于Redis则表示缓存名称,对于MongoDB则是集合名称，以此类推)
	 */
	private String tableName = "seq_registry";

	/**
	 * 每次从后端取值的步进,这个值需要权衡性能和序号丢失
	 */
	private int fetchSize = 1000;

	/**
	 * 名称
	 */
	private String name = "seq";

	/**
	 * 起始值
	 */
	private long startValue = 1L;

	public enum BackendTypeEnum {

		/**
		 * MySQL
		 */
		mysql,
		/**
		 * Oracle
		 */
		oracle

	}

}
