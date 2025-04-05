package com.games.balancegameback.core.utils;

import lombok.Getter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
public class CustomBasedPageImpl<T> extends PageImpl<T> {

    private final boolean hasPrev;
    private final boolean hasNext;

    public CustomBasedPageImpl(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);

        int currentPage = pageable.getPageNumber();
        int totalPages = (int) Math.ceil((double) total / pageable.getPageSize());

        this.hasPrev = currentPage > 0;
        this.hasNext = currentPage + 1 < totalPages;
    }
}