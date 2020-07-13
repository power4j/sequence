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

package com.power4j.kit.seq.persistent.provider;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/13
 * @since 1.1
 */
public interface RedisConstants {

	// @formatter:off

	String KEY_DELIMITER =  ":";

	String UPDATE_SCRIPT = "" +
			"local key = KEYS[1]\n" +
			"local old_val = ARGV[1]\n" +
			"local new_val = ARGV[2]\n" +
			"\n" +
			"local val = redis.call(\"GET\", key)\n" +
			"if val == old_val then\n" +
			"    redis.call(\"SET\", key, new_val)\n" +
			"    return true\n" +
			"else\n" +
			"    return false\n" +
			"end";

	// @formatter:on

}
