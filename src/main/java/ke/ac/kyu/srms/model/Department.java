
package ke.ac.kyu.srms.model;

import java.util.Objects;
import java.util.UUID;

public class Department {

    private final String id;
    private String name;
    private String description;

    public Department(String name, String description) {
        this(UUID.randomUUID().toString(), name, description);
    }

    public Department(String id, String name, String description) {
        this.id = Objects.requireNonNull(id, "Department ID cannot be null");
        setName(name);
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
            throw new IllegalArgumentException("Department name is required.");
        }
        this.name = name.trim().replaceAll("\\s+", " ");
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
