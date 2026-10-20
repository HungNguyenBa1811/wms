package com.hung.wms.dto.response.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ErrorResponse {
    private LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> detail = new ArrayList<>();

    // Extra data from AppException (e.g. current status of a purchase order), omitted from the JSON when null
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;
}
