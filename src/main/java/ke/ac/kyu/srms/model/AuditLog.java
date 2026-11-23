
package ke.ac.kyu.srms.model;

import java.time.LocalDateTime;

public record AuditLog(
        long id,
        Integer adminUserId,
        String adminUsername,
        String action,
        String entityType,
        String entityId,
        String details,
        LocalDateTime createdAt
) {
}
