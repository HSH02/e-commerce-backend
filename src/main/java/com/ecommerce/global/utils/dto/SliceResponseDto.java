package com.ecommerce.global.utils.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@Builder
public class SliceResponseDto<T> {
    private List<T> content;            // 내용
    private int currentPage;            // 현재 페이지
    private int size;                   // 크기
    private boolean hasNext;            // 다음 페이지 존재 여부
    private boolean hasPrevious;        // 이전 페이지 존재 여부

    @JsonProperty("isFirst")
    private boolean isFirst;            // 첫 번째 페이지 여부

    @JsonProperty("isLast")
    private boolean isLast;             // 마지막 페이지 여부
    private int numberOfElements;       // 현재 페이지의 실제 데이터 개수

    public static <T> SliceResponseDto<T> from(Slice<T> slice) {
        return SliceResponseDto.<T>builder()
                .content(slice.getContent())
                .currentPage(slice.getNumber())
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .hasPrevious(slice.hasPrevious())
                .isFirst(slice.isFirst())
                .isLast(slice.isLast())
                .numberOfElements(slice.getNumberOfElements())
                .build();
    }

    // 빈 결과를 위한 정적 팩토리 메서드
    public static <T> SliceResponseDto<T> empty() {
        return SliceResponseDto.<T>builder()
                .content(List.of())
                .currentPage(0)
                .size(0)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .numberOfElements(0)
                .build();
    }
}