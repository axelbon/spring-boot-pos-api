package com.axelbon.pos.service.impl;

import org.springframework.stereotype.Service;

import com.axelbon.pos.entity.AuditLog;
import com.axelbon.pos.repository.AuditLogRepository;
import com.axelbon.pos.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void saveAuditLog(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }
    
}
