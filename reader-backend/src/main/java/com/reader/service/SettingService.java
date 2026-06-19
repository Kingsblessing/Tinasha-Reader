package com.reader.service;

import com.reader.model.entity.Setting;
import com.reader.repository.SettingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SettingService {

    private final SettingRepository settingRepository;

    public SettingService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public Map<String, String> findAll() {
        List<Setting> settings = settingRepository.findAll();
        Map<String, String> map = new java.util.LinkedHashMap<>();
        for (Setting s : settings) {
            map.put(s.getKey(), s.getValue());
        }
        return map;
    }

    public String get(String key) {
        Setting setting = settingRepository.findByKey(key);
        return setting != null ? setting.getValue() : null;
    }

    public String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    public void set(String key, String value) {
        Setting setting = new Setting();
        setting.setKey(key);
        setting.setValue(value);
        settingRepository.save(setting);
    }
}
