package com.smartpos.service;

import com.smartpos.model.AuditLog;
import com.smartpos.model.User;
import com.smartpos.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private com.smartpos.util.AppSession session;

    public void log(User user, String action, String details, String entityName, Long entityId) {
        AuditLog entry = new AuditLog();
        entry.setTenant(session.getCurrentTenant());
        entry.setUser(user);
        entry.setAction(action);
        entry.setDetails(details);
        entry.setEntityName(entityName);
        entry.setEntityId(entityId);
        auditLogRepository.save(entry);
    }

    public List<AuditLog> findAll() {
        return auditLogRepository.findByTenantId(session.getCurrentTenant().getId());
    }

    public List<AuditLog> findLatest(int limit) {
        return findAll().stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .toList();
    }
}
