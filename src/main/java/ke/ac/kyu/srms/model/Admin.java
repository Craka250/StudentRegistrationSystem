
package ke.ac.kyu.srms.model;

import java.time.LocalDateTime;

public class Admin {

    private final int id;
    private final String username;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastLoginAt;

    public Admin(
            int id,
            String username,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime lastLoginAt
    ) {
        this.id = id;
        this.username = username;
        this.active = active;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}
