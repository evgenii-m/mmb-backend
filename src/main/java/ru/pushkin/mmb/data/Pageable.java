package ru.pushkin.mmb.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Pageable<T> {
    private final Integer page;
    private final Integer size;
    private final Long totalSize;
    private final List<T> data;

    public static <T> Pageable<T> empty() {
        return new Pageable<T>(0, 0, 0L, new ArrayList<T>());
    }
}
