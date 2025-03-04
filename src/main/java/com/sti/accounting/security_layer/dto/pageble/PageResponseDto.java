package com.sti.accounting.security_layer.dto.pageble;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@JsonSerialize
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDto<T> implements PageResponse<T> {

    @JsonProperty("pageSize")
    private int pageSize;


    @JsonProperty("totalPages")
    private int totalPages;

    @JsonProperty("pageNumber")
    private int pageNumber;

    @JsonProperty("response")
    private List<T> response;

    @Override
    public Integer getPageSize() {
        return this.pageSize;
    }

    @Override
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }


    @Override
    public int getTotalPages() {
        return this.totalPages;
    }

    @Override
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    @Override
    public Integer getPageNumber() {
        return this.pageNumber;
    }

    @Override
    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Override
    public List<T> getResponse() {
        return this.response;
    }

    @Override
    public void setResponse(List<T> response) {
        this.response = response;
    }

}