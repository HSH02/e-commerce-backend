package com.ecommerce.global.utils.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@Builder
public class SliceResponseDto<T> {
    private List<T> content;
    private int currentPage;
    private int size;
    private boolean hasNext;
    
    public static <T> SliceResponseDto<T> from(Slice<T> slice) {
        return SliceResponseDto.<T>builder()
                .content(slice.getContent())
                .currentPage(slice.getNumber())
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .build();
    }
}