package com.reader.controller;

import com.reader.common.Result;
import com.reader.service.SettingService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping
    public Result<Map<String, String>> findAll() {
        return Result.success(settingService.findAll());
    }

    @GetMapping("/{key}")
    public Result<String> get(@PathVariable String key) {
        String value = settingService.get(key);
        return Result.success(value);
    }

    @PutMapping("/{key}")
    public Result<Void> set(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");
        settingService.set(key, value);
        return Result.success();
    }
}
