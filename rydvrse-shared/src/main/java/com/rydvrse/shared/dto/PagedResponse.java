package com.rydvrse.shared.dto;

import lombok.*;

import java.util.List;

/**
 * Standardized paginated response wrapper.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
