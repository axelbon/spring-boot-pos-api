package com.axelbon.pos.service;

import com.axelbon.pos.entity.AuditLog;

public interface AuditLogService {
    void saveAuditLog(AuditLog auditLog);
}
