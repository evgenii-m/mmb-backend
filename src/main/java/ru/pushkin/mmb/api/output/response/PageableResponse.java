package ru.pushkin.mmb.api.output.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageableResponse<T> {
    private int page;
    private int size;
    private long totalSize;
    private List<T> data;
}
