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

package com.power4j.kit.seq.core.exceptions;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/6/30
 * @since 1.0
 */
public class SeqException extends RuntimeException {

	public SeqException(String message) {
		super(message);
	}

	public SeqException(String message, Throwable cause) {
		super(message, cause);
	}

	public SeqException(Throwable cause) {
		super(cause);
	}

}
