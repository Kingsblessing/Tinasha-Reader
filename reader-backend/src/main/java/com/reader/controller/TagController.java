package com.reader.controller;

import com.reader.common.Result;
import com.reader.model.dto.TagCreateDTO;
import com.reader.model.entity.Tag;
import com.reader.service.TagService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public Result<List<Tag>> findAll() {
        return Result.success(tagService.findAll());
    }

    @PostMapping
    public Result<Tag> create(@Valid @RequestBody TagCreateDTO dto) {
        return Result.success(tagService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Tag> update(@PathVariable long id, @RequestBody Tag tag) {
        return Result.success(tagService.update(id, tag));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable long id) {
        tagService.delete(id);
        return Result.success();
    }

    @GetMapping("/groups")
    public Result<List<String>> getGroupNames() {
        return Result.success(tagService.getGroupNames());
    }

    @PostMapping("/comics/{comicId}/tags")
    public Result<Void> addTagToComic(@PathVariable long comicId, @RequestBody Map<String, Long> body) {
        long tagId = body.get("tagId");
        tagService.addTagToComic(comicId, tagId);
        return Result.success();
    }

    @DeleteMapping("/comics/{comicId}/tags/{tagId}")
    public Result<Void> removeTagFromComic(@PathVariable long comicId, @PathVariable long tagId) {
        tagService.removeTagFromComic(comicId, tagId);
        return Result.success();
    }

    @GetMapping("/comics/{comicId}/tags")
    public Result<List<Tag>> findByComicId(@PathVariable long comicId) {
        return Result.success(tagService.findByComicId(comicId));
    }
}
