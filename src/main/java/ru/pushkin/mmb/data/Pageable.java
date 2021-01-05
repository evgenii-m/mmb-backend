package ru.pushkin.mmb.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Pageable<T> {
    private final long page;
    private final long size;
    private final long totalPages;
    private final long totalSize;
    private final List<T> data;

    public static <T> Pageable<T> empty() {
        return new Pageable<T>(0L, 0L, 0L, 0L, new ArrayList<T>());
    }
}
