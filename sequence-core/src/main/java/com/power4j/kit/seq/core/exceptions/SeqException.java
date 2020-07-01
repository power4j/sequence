package com.power4j.kit.seq.core.exceptions;

/**
 * @author CJ (jclazz@outlook.com)
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
