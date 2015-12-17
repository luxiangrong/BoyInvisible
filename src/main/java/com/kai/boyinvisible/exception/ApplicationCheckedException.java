package com.kai.boyinvisible.exception;

import org.springframework.core.NestedCheckedException;

public class ApplicationCheckedException extends NestedCheckedException {

	public ApplicationCheckedException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public ApplicationCheckedException(String msg) {
		super(msg);
	}

	private static final long serialVersionUID = 9146019332829864896L;

}
