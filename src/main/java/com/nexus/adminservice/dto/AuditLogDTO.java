package com.nexus.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private String id;
    private String actorId;
    private String action;
    private String targetId;
    private String details;
    private LocalDateTime createdAt;
}