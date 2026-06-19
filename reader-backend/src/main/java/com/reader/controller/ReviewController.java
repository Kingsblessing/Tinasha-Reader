package com.reader.controller;

import com.reader.common.Result;
import com.reader.model.dto.ReviewCreateDTO;
import com.reader.model.entity.Review;
import com.reader.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/comics/{comicId}/reviews")
    public Result<List<Review>> findByComicId(@PathVariable long comicId) {
        return Result.success(reviewService.findByComicId(comicId));
    }

    @PostMapping("/comics/{comicId}/reviews")
    public Result<Review> create(@PathVariable long comicId, @Valid @RequestBody ReviewCreateDTO dto) {
        dto.setComicId(comicId);
        return Result.success(reviewService.create(dto));
    }

    @PutMapping("/reviews/{id}")
    public Result<Review> update(@PathVariable long id, @RequestBody Map<String, String> body) {
        String content = body.get("content");
        return Result.success(reviewService.update(id, content));
    }

    @DeleteMapping("/reviews/{id}")
    public Result<Void> delete(@PathVariable long id) {
        reviewService.delete(id);
        return Result.success();
    }
}
