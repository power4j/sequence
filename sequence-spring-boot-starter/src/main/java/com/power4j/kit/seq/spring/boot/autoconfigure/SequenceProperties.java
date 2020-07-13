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

package com.power4j.kit.seq.spring.boot.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/6
 * @since 1.0
 */
@Data
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
	private int fetchSize = 100;

	/**
	 * 名称
	 */
	private String name = "seq";

	/**
	 * 起始值
	 */
	private long startValue = 1L;

	/**
	 * Lettuce URI
	 * <a>https://lettuce.io/core/release/reference/index.html#redisuri.uri-syntax</a>
	 */
	private String lettuceUri = "redis://localhost";

	public enum BackendTypeEnum {

		/**
		 * MySQL
		 */
		mysql,
		/**
		 * Redis
		 */
		redis,
		/**
		 * Redis Cluster
		 */
		redisCluster

	}

}
