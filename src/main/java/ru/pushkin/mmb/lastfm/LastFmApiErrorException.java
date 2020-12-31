package ru.pushkin.mmb.lastfm;

public class LastFmApiErrorException extends RuntimeException {

    public LastFmApiErrorException() {
        super();
    }

    public LastFmApiErrorException(String message) {
        super(message);
    }
}
