package com.sti.accounting.security_layer.dto.pageble;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PageResponse<T> {

    Integer getPageSize();
    void setPageSize(Integer pageSize);


    int getTotalPages();
    void setTotalPages(Integer totalPages);

    Integer getPageNumber();
    void setPageNumber(Integer pageNumber);

    List<T> getResponse();
    void setResponse(List<T> pagePayload);

    default ResponseEntity<? extends PageResponse<T>> buildResponseEntity
            (Integer pageSize, Integer numberOfElements, Integer totalPages,
             Integer pageNumber, List<T> response){
        this.setPageSize(pageSize);
        this.setTotalPages(totalPages);
        this.setPageNumber(pageNumber);
        this.setResponse(response);
        return new ResponseEntity<>(this, HttpStatus.OK);
    }

}