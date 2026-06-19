package com.reader.controller;

import com.reader.common.Result;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/api/v1/filesystem")
public class FileBrowserController {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp"
    );
    private static final Set<String> COMIC_EXTENSIONS = Set.of(
            ".cbz", ".cbr", ".zip", ".rar", ".7z", ".pdf"
    );

    @GetMapping("/browse")
    public Result<Map<String, Object>> browse(@RequestParam(defaultValue = "") String path) {
        File dir;
        if (path.isEmpty()) {
            // List drives on Windows
            File[] roots = File.listRoots();
            List<Map<String, Object>> items = new ArrayList<>();
            for (File root : roots) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", root.getAbsolutePath());
                item.put("path", root.getAbsolutePath());
                item.put("isDirectory", true);
                item.put("type", "drive");
                items.add(item);
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("currentPath", "");
            result.put("parentPath", null);
            result.put("items", items);
            return Result.success(result);
        }

        dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            return Result.error(400, "目录不存在: " + path);
        }

        File[] files = dir.listFiles();
        List<Map<String, Object>> items = new ArrayList<>();
        if (files != null) {
            Arrays.sort(files, (a, b) -> {
                // Directories first, then files
                if (a.isDirectory() != b.isDirectory()) {
                    return a.isDirectory() ? -1 : 1;
                }
                return a.getName().compareToIgnoreCase(b.getName());
            });
            for (File f : files) {
                if (f.isHidden()) continue;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", f.getName());
                item.put("path", f.getAbsolutePath());
                item.put("isDirectory", f.isDirectory());
                if (!f.isDirectory()) {
                    item.put("size", f.length());
                    String ext = getExtension(f.getName());
                    if (IMAGE_EXTENSIONS.contains(ext)) {
                        item.put("type", "image");
                    } else if (COMIC_EXTENSIONS.contains(ext)) {
                        item.put("type", "comic");
                    } else {
                        item.put("type", "file");
                    }
                } else {
                    item.put("type", "folder");
                }
                items.add(item);
            }
        }

        File parentDir = dir.getParentFile();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("currentPath", dir.getAbsolutePath());
        result.put("parentPath", parentDir != null ? parentDir.getAbsolutePath() : null);
        result.put("items", items);
        return Result.success(result);
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot).toLowerCase() : "";
    }
}
