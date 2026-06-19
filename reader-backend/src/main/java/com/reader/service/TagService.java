package com.reader.service;

import com.reader.common.BusinessException;
import com.reader.common.ErrorCode;
import com.reader.model.dto.TagCreateDTO;
import com.reader.model.entity.Tag;
import com.reader.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    public Tag findById(long id) {
        Tag tag = tagRepository.findById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND, "Tag not found: " + id);
        }
        return tag;
    }

    public Tag create(TagCreateDTO dto) {
        Tag tag = new Tag();
        tag.setName(dto.getName());
        tag.setGroupName(dto.getGroupName() != null ? dto.getGroupName() : "自定义");
        tag.setColor(dto.getColor());
        tag.setParentId(dto.getParentId());
        tag.setComicCount(0);

        long id = tagRepository.save(tag);
        tag.setId(id);
        return tag;
    }

    public Tag update(long id, Tag tag) {
        Tag existing = findById(id);
        existing.setName(tag.getName());
        if (tag.getGroupName() != null) existing.setGroupName(tag.getGroupName());
        if (tag.getColor() != null) existing.setColor(tag.getColor());
        existing.setParentId(tag.getParentId());
        tagRepository.update(existing);
        return existing;
    }

    public void delete(long id) {
        findById(id); // validate exists
        tagRepository.deleteById(id);
    }

    public List<Tag> findByComicId(long comicId) {
        return tagRepository.findByComicId(comicId);
    }

    public void addTagToComic(long comicId, long tagId) {
        findById(tagId); // validate tag exists
        tagRepository.addComicTag(comicId, tagId);
        tagRepository.incrementComicCount(tagId, 1);
    }

    public void removeTagFromComic(long comicId, long tagId) {
        findById(tagId); // validate tag exists
        tagRepository.removeComicTag(comicId, tagId);
        tagRepository.incrementComicCount(tagId, -1);
    }

    public List<String> getGroupNames() {
        return tagRepository.findGroupNames();
    }
}
