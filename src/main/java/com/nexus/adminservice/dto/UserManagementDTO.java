package com.nexus.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementDTO {
    private String id;
    private String email;
    private String fullName;
    private String indexNumber;
    private String role;
    private boolean enabled;
}