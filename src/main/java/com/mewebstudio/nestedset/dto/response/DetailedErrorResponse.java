package com.mewebstudio.nestedset.dto.response;

import java.util.Map;

public class DetailedErrorResponse extends ErrorResponse {
    private String message;

    private Map<String, String> items;

    public DetailedErrorResponse(String message, Map<String, String> items) {
        super(message);
        this.message = message;
        this.items = items;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getItems() {
        return items;
    }

    public void setItems(Map<String, String> items) {
        this.items = items;
    }
}
