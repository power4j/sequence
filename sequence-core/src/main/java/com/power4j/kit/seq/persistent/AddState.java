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

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 加法操作执行结果
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/2
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class AddState {

	/**
	 * 执行结果
	 */
	private boolean success;

	/**
	 * 前一个值
	 */
	private Long previous;

	/**
	 * 当前值
	 */
	private Long current;

	/**
	 * 操作次数
	 */
	private int totalOps;

	public static AddState fail(int totalOps) {
		return new AddState(false, null, null, totalOps);
	}

	public static AddState success(long previous, long current, int totalOps) {
		return new AddState(true, previous, current, totalOps);
	}

}
