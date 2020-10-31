package ru.pushkin.mma.deezer;

public class DeezerApiErrorException extends Exception {

	public DeezerApiErrorException() {
		super();
	}

	public DeezerApiErrorException(String message) {
		super(message);
	}
}
