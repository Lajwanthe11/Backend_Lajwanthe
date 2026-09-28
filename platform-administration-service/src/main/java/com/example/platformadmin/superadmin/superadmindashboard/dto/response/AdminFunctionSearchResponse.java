package com.example.platformadmin.superadmin.superadmindashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFunctionSearchResponse {
    private String functionName;
    private String module;
    private String description;
    private String apiEndpoint;
    private String uiRoute;
    private List<String> keywords;
}
