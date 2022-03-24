package com.epam.model.testcycle;

import lombok.Builder;

@Builder
public class TestCycleBuilder {

    private String name;
    private String description;
    private Integer jiraProjectVersion;
    private Integer folderId;
    private String customFields;


    @Override
    public String toString() {
        return String.format("{\"name\":\"%s\", \"description\":\"%s\", \"jiraProjectVersion\":%s, \"folderId\":%s, \"customFields\":%s}",
                name, description, jiraProjectVersion, folderId, customFields);
    }
}
