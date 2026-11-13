
package ke.ac.kyu.srms.model;

import java.util.Objects;
import java.util.UUID;

public class Programme {

    private final String id;
    private String name;
    private String departmentId;
    private String description;

    public Programme(String name, String departmentId, String description) {
        this(UUID.randomUUID().toString(), name, departmentId, description);
    }

    public Programme(
            String id,
            String name,
            String departmentId,
            String description
    ) {
        this.id = Objects.requireNonNull(id, "Programme ID cannot be null");
        setName(name);
        setDepartmentId(departmentId);
        setDescription(description);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Programme name is required.");
        }
        this.name = name.trim().replaceAll("\\s+", " ");
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        if (departmentId == null || departmentId.isBlank()) {
            throw new IllegalArgumentException("A department must be selected.");
        }
        this.departmentId = departmentId.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    @Override
    public String toString() {
        return name;
    }
}
