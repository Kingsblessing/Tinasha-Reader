package com.reader.service;

import com.reader.common.BusinessException;
import com.reader.common.ErrorCode;
import com.reader.common.PageResult;
import com.reader.model.dto.ComicUpdateDTO;
import com.reader.model.entity.Comic;
import com.reader.model.entity.Page;
import com.reader.model.entity.Source;
import com.reader.model.entity.Tag;
import com.reader.model.vo.ComicDetailVO;
import com.reader.model.vo.ComicVO;
import com.reader.model.vo.TagVO;
import com.reader.repository.ComicRepository;
import com.reader.repository.FavoriteRepository;
import com.reader.repository.PageRepository;
import com.reader.repository.SourceRepository;
import com.reader.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ComicService {

    private final ComicRepository comicRepository;
    private final PageRepository pageRepository;
    private final SourceRepository sourceRepository;
    private final TagRepository tagRepository;
    private final FavoriteRepository favoriteRepository;

    public ComicService(ComicRepository comicRepository,
                        PageRepository pageRepository,
                        SourceRepository sourceRepository,
                        TagRepository tagRepository,
                        FavoriteRepository favoriteRepository) {
        this.comicRepository = comicRepository;
        this.pageRepository = pageRepository;
        this.sourceRepository = sourceRepository;
        this.tagRepository = tagRepository;
        this.favoriteRepository = favoriteRepository;
    }

    public PageResult<Comic> findAll(String search, Long sourceId, String status,
                                      List<Long> tagIds, String sort, int page, int size) {
        return comicRepository.findAll(search, sourceId, status, tagIds, sort, page, size);
    }

    public Comic findById(long id) {
        Comic comic = comicRepository.findById(id);
        if (comic == null) {
            throw new BusinessException(ErrorCode.COMIC_NOT_FOUND, "Comic not found: " + id);
        }
        return comic;
    }

    public ComicDetailVO findByIdWithDetails(long id) {
        Comic comic = findById(id);
        ComicDetailVO vo = new ComicDetailVO();

        // Copy base fields
        vo.setId(comic.getId());
        vo.setTitle(comic.getTitle());
        vo.setAuthor(comic.getAuthor());
        vo.setCoverPath(comic.getCoverPath());
        vo.setPageCount(comic.getPageCount());
        vo.setRating(comic.getRating());
        vo.setStatus(comic.getStatus());
        vo.setLastReadAt(comic.getLastReadAt());
        vo.setLastReadPage(comic.getLastReadPage());
        vo.setCreatedAt(comic.getCreatedAt());

        // Detail fields
        vo.setPublisher(comic.getPublisher());
        vo.setDescription(comic.getDescription());
        vo.setLanguage(comic.getLanguage());
        vo.setFileSize(comic.getFileSize());
        vo.setReadCount(comic.getReadCount());
        vo.setFileType(comic.getFileType());

        // Source name
        Source source = sourceRepository.findById(comic.getSourceId());
        if (source != null) {
            vo.setSourceName(source.getName());
        }

        // Tags
        List<Tag> tags = tagRepository.findByComicId(comic.getId());
        List<TagVO> tagVOs = new ArrayList<>();
        for (Tag tag : tags) {
            TagVO tagVO = new TagVO();
            tagVO.setId(tag.getId());
            tagVO.setName(tag.getName());
            tagVO.setGroupName(tag.getGroupName());
            tagVO.setColor(tag.getColor());
            tagVOs.add(tagVO);
        }
        vo.setTags(tagVOs);

        // Favorite status
        boolean isFavorited = !favoriteRepository.findByComicId(comic.getId()).isEmpty();
        vo.setIsFavorited(isFavorited);

        return vo;
    }

    public Comic update(long id, ComicUpdateDTO dto) {
        Comic comic = findById(id);
        if (dto.getTitle() != null) comic.setTitle(dto.getTitle());
        if (dto.getAuthor() != null) comic.setAuthor(dto.getAuthor());
        if (dto.getPublisher() != null) comic.setPublisher(dto.getPublisher());
        if (dto.getDescription() != null) comic.setDescription(dto.getDescription());
        if (dto.getStatus() != null) comic.setStatus(dto.getStatus());
        if (dto.getRating() != null) comic.setRating(dto.getRating());
        comicRepository.update(comic);
        return comic;
    }

    public void delete(long id) {
        findById(id); // validate exists
        pageRepository.deleteByComicId(id);
        comicRepository.deleteById(id);
    }

    public Comic updateRating(long id, BigDecimal rating) {
        Comic comic = findById(id);
        comic.setRating(rating);
        comicRepository.updateRating(id, rating);
        comic.setRating(rating);
        return comic;
    }

    public void updateReadProgress(long id, int page) {
        findById(id); // validate exists
        comicRepository.updateReadProgress(id, page);
    }

    public List<Page> getPages(long comicId) {
        findById(comicId); // validate exists
        return pageRepository.findByComicId(comicId);
    }

    public Page getPage(long comicId, int pageNum) {
        findById(comicId); // validate exists
        Page page = pageRepository.findByComicIdAndPageNumber(comicId, pageNum);
        if (page == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "Page not found: " + pageNum);
        }
        return page;
    }

    public List<Comic> getRecentRead(int limit) {
        return comicRepository.findRecentRead(limit);
    }

    public Comic getNextComic(long comicId) {
        Comic comic = findById(comicId);
        Comic next = comicRepository.findNextInSource(comicId, comic.getSourceId());
        if (next == null) {
            throw new BusinessException(ErrorCode.COMIC_NOT_FOUND, "No next comic found");
        }
        return next;
    }

    public Comic getPrevComic(long comicId) {
        Comic comic = findById(comicId);
        Comic prev = comicRepository.findPrevInSource(comicId, comic.getSourceId());
        if (prev == null) {
            throw new BusinessException(ErrorCode.COMIC_NOT_FOUND, "No previous comic found");
        }
        return prev;
    }
}
