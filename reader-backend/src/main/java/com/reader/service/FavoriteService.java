package com.reader.service;

import com.reader.common.BusinessException;
import com.reader.common.ErrorCode;
import com.reader.model.entity.Favorite;
import com.reader.repository.ComicRepository;
import com.reader.repository.FavoriteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ComicRepository comicRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           ComicRepository comicRepository) {
        this.favoriteRepository = favoriteRepository;
        this.comicRepository = comicRepository;
    }

    public List<Favorite> findAll() {
        return favoriteRepository.findAll();
    }

    public List<Favorite> findByGroupName(String group) {
        return favoriteRepository.findByGroupName(group);
    }

    public Favorite create(long comicId, String groupName) {
        // Validate comic exists
        if (comicRepository.findById(comicId) == null) {
            throw new BusinessException(ErrorCode.COMIC_NOT_FOUND, "Comic not found: " + comicId);
        }

        String group = (groupName != null && !groupName.isEmpty()) ? groupName : "默认";

        // Check for duplicates
        if (favoriteRepository.existsByComicIdAndGroupName(comicId, group)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Comic already favorited in this group");
        }

        Favorite favorite = new Favorite();
        favorite.setComicId(comicId);
        favorite.setGroupName(group);

        long id = favoriteRepository.save(favorite);
        favorite.setId(id);
        return favorite;
    }

    public void delete(long id) {
        Favorite favorite = favoriteRepository.findById(id);
        if (favorite == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Favorite not found: " + id);
        }
        favoriteRepository.deleteById(id);
    }

    public List<Favorite> findByComicId(long comicId) {
        return favoriteRepository.findByComicId(comicId);
    }

    public boolean isFavorited(long comicId) {
        return !favoriteRepository.findByComicId(comicId).isEmpty();
    }

    public List<String> getGroupNames() {
        return favoriteRepository.findGroupNames();
    }
}
