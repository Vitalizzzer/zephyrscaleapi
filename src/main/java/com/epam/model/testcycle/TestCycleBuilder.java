package com.epam.model.testcycle;

import lombok.Builder;

@Builder
public class TestCycleBuilder {

    private String name;
    private String description;
    private Integer jiraProjectVersion;
    private Integer folderId;

    @Override
    public String toString() {
        return String.format("{\"name\":\"%s\", \"description\":\"%s\", \"jiraProjectVersion\":%s, \"folderId\":%s}",
                name, description, jiraProjectVersion, folderId);
    }
}
