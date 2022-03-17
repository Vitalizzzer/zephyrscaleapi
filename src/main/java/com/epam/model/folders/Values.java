package com.epam.model.folders;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Values {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("parentId")
    private Integer parentId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("index")
    private Integer index;

    @JsonProperty("folderType")
    private String folderType;

    @JsonProperty("project")
    private Project project;

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o.getClass() != this.getClass()) {
            return false;
        }

        final Values other = (Values) o;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }

        if (!this.id.equals(other.id)) {
            return false;
        }

        if (!this.folderType.equals(other.folderType)) {
            return false;
        }

        if (!this.parentId.equals(other.parentId)) {
            return false;
        }

        if (!this.index.equals(other.index)) {
            return false;
        }

        return this.project.equals(other.project);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 53 * hash + this.id;
        hash = 53 * hash + (this.folderType != null ? this.folderType.hashCode() : 0);
        hash = 53 * hash + this.parentId;
        hash = 53 * hash + this.index;
        hash = 53 * hash + (this.project != null ? this.project.hashCode() : 0);

        return hash;
    }
}
