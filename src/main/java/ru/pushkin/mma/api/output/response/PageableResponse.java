package ru.pushkin.mma.api.output.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PageableResponse<T> {
    private int page;
    private int size;
    private int totalSize;
    private T data;
}
