package ru.pushkin.mma.deezer;

public class DeezerApiErrorException extends RuntimeException {

	public DeezerApiErrorException() {
		super();
	}

	public DeezerApiErrorException(String message) {
		super(message);
	}
}
