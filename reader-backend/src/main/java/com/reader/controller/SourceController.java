package com.reader.controller;

import com.reader.common.Result;
import com.reader.model.dto.SourceCreateDTO;
import com.reader.model.entity.Source;
import com.reader.service.ScannerService;
import com.reader.service.SourceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sources")
public class SourceController {

    private final SourceService sourceService;
    private final ScannerService scannerService;

    public SourceController(SourceService sourceService, ScannerService scannerService) {
        this.sourceService = sourceService;
        this.scannerService = scannerService;
    }

    @GetMapping
    public Result<List<Source>> findAll() {
        return Result.success(sourceService.findAll());
    }

    @PostMapping
    public Result<Source> create(@Valid @RequestBody SourceCreateDTO dto) {
        return Result.success(sourceService.create(dto));
    }

    @GetMapping("/{id}")
    public Result<Source> findById(@PathVariable long id) {
        return Result.success(sourceService.findById(id));
    }

    @PutMapping("/{id}")
    public Result<Source> update(@PathVariable long id, @RequestBody Source source) {
        return Result.success(sourceService.update(id, source));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable long id) {
        sourceService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/scan")
    public Result<Void> scan(@PathVariable long id) {
        Source source = sourceService.findById(id);
        // Run scan asynchronously in a new thread
        Thread scanThread = new Thread(() -> scannerService.scanSource(source));
        scanThread.setDaemon(true);
        scanThread.start();
        return Result.success();
    }

    @GetMapping("/{id}/scan-status")
    public Result<Map<String, String>> scanStatus(@PathVariable long id) {
        Source source = sourceService.findById(id);
        return Result.success(Map.of(
                "scanStatus", source.getScanStatus() != null ? source.getScanStatus() : "IDLE",
                "lastScanAt", source.getLastScanAt() != null ? source.getLastScanAt().toString() : ""
        ));
    }

    @PutMapping("/{id}/toggle")
    public Result<Source> toggleEnabled(@PathVariable long id) {
        return Result.success(sourceService.toggleEnabled(id));
    }
}
