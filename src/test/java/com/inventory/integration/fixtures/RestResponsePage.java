package com.inventory.integration.fixtures;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RestResponsePage<T> extends PageImpl<T> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestResponsePage(@JsonProperty("content") List<T> content,
                           @JsonProperty("number") int number,
                           @JsonProperty("size") int size,
                           @JsonProperty("totalElements") Long totalElements,
                           @JsonProperty("pageable") JsonPageable pageable,
                           @JsonProperty("last") boolean last,
                           @JsonProperty("totalPages") int totalPages,
                           @JsonProperty("sort") JsonSort sort,
                           @JsonProperty("first") boolean first,
                           @JsonProperty("numberOfElements") int numberOfElements) {
        super(content, PageRequest.of(number, size), totalElements);
    }

    public RestResponsePage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public RestResponsePage(List<T> content) {
        super(content);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JsonPageable {
        @JsonProperty("offset")
        public int offset;
        @JsonProperty("sort")
        public JsonSort sort;
        @JsonProperty("pageNumber")
        public int pageNumber;
        @JsonProperty("pageSize")
        public int pageSize;
        @JsonProperty("paged")
        public boolean paged;
        @JsonProperty("unpaged")
        public boolean unpaged;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JsonSort {
        @JsonProperty("empty")
        public boolean empty;
        @JsonProperty("sorted")
        public boolean sorted;
        @JsonProperty("unsorted")
        public boolean unsorted;
    }
}