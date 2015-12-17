package com.kai.boyinvisible.exception;

import org.springframework.core.NestedRuntimeException;

public class ApplicationRuntimeException extends NestedRuntimeException {

	private static final long serialVersionUID = -2063881297540181900L;

	public ApplicationRuntimeException(String msg) {
		super(msg);
	}

	public ApplicationRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
