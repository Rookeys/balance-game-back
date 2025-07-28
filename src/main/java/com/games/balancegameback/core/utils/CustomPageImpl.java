package com.games.balancegameback.core.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
public class CustomPageImpl<T> extends PageImpl<T> {

    private final boolean hasPrev;
    private final boolean hasNext;
    private final long totalElements;

    public CustomPageImpl(List<T> content, Pageable pageable, Long totalElements, Long cursorId, boolean hasNext) {
        super(content, pageable, totalElements);

        this.hasPrev = cursorId != null;
        this.hasNext = hasNext;
        this.totalElements = totalElements;
    }

    @Override
    public long getTotalElements() {
        return this.totalElements;
    }

    @JsonProperty("totalElements")
    public long getTotalElementsJson() {
        return this.totalElements;
    }
}


