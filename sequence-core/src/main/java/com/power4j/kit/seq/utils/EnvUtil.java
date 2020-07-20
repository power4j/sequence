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

package com.power4j.kit.seq.utils;

import lombok.experimental.UtilityClass;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/20
 * @since 1.0
 */
@UtilityClass
public class EnvUtil {

	public static String getStr(String envKey, String defValue) {
		String val = System.getenv(envKey);
		return val == null ? defValue : val;
	}

	public static Integer getInt(String envKey, Integer defValue) {
		String val = getStr(envKey, null);
		if (val == null) {
			return defValue;
		}
		try {
			return Integer.parseInt(val);
		}
		catch (NumberFormatException e) {
			return defValue;
		}
	}

}
