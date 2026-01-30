package com.smartpos.service;

import com.smartpos.model.SystemConfig;
import com.smartpos.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class SystemConfigService {

    @Autowired
    private SystemConfigRepository repository;

    public String getConfig(String key, String defaultValue) {
        return repository.findById(key)
                .map(SystemConfig::getConfigValue)
                .orElse(defaultValue);
    }

    public void setConfig(String key, String value) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        repository.save(config);
    }
}
