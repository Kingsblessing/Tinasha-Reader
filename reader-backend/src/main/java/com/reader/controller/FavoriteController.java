package com.reader.controller;

import com.reader.common.Result;
import com.reader.model.entity.Favorite;
import com.reader.service.FavoriteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public Result<List<Favorite>> findAll(@RequestParam(required = false) String groupName) {
        if (groupName != null && !groupName.isEmpty()) {
            return Result.success(favoriteService.findByGroupName(groupName));
        }
        return Result.success(favoriteService.findAll());
    }

    @GetMapping("/check")
    public Result<Map<String, Object>> checkFavorited(@RequestParam long comicId) {
        List<Favorite> favorites = favoriteService.findByComicId(comicId);
        if (favorites.isEmpty()) {
            return Result.success(Map.of("favorited", false, "favoriteId", -1));
        }
        Favorite fav = favorites.get(0);
        return Result.success(Map.of("favorited", true, "favoriteId", fav.getId()));
    }

    @PostMapping
    public Result<Favorite> create(@RequestBody Map<String, Object> body) {
        long comicId = ((Number) body.get("comicId")).longValue();
        String groupName = (String) body.get("groupName");
        return Result.success(favoriteService.create(comicId, groupName));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable long id) {
        favoriteService.delete(id);
        return Result.success();
    }

    @GetMapping("/groups")
    public Result<List<String>> getGroupNames() {
        return Result.success(favoriteService.getGroupNames());
    }
}
