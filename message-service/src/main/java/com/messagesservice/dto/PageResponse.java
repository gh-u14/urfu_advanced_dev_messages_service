package com.messagesservice.dto;

import java.util.List;

public class PageResponse<T> {

    private List<T> items;
    private int limit;
    private int offset;

    public PageResponse() {
    }

    public PageResponse(List<T> items, int limit, int offset) {
        this.items = items;
        this.limit = limit;
        this.offset = offset;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
