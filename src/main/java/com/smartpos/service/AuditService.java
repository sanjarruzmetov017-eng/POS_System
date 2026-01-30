package com.smartpos.service;

import com.smartpos.model.AuditLog;
import com.smartpos.model.User;
import com.smartpos.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(User user, String action, String details, String entityName, Long entityId) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setDetails(details);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        auditLogRepository.save(log);
        System.out.println("🛡️ Audit Log: [" + action + "] " + details);
    }
}
