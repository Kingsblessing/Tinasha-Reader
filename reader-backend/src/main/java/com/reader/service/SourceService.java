package com.reader.service;

import com.reader.common.BusinessException;
import com.reader.common.ErrorCode;
import com.reader.model.dto.SourceCreateDTO;
import com.reader.model.entity.Comic;
import com.reader.model.entity.Source;
import com.reader.repository.ComicRepository;
import com.reader.repository.PageRepository;
import com.reader.repository.SourceRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class SourceService {

    private final SourceRepository sourceRepository;
    private final ComicRepository comicRepository;
    private final PageRepository pageRepository;

    public SourceService(SourceRepository sourceRepository,
                         ComicRepository comicRepository,
                         PageRepository pageRepository) {
        this.sourceRepository = sourceRepository;
        this.comicRepository = comicRepository;
        this.pageRepository = pageRepository;
    }

    public List<Source> findAll() {
        return sourceRepository.findAll();
    }

    public Source findById(long id) {
        Source source = sourceRepository.findById(id);
        if (source == null) {
            throw new BusinessException(ErrorCode.SOURCE_NOT_FOUND, "Source not found: " + id);
        }
        return source;
    }

    public Source create(SourceCreateDTO dto) {
        Path path = Path.of(dto.getPath());
        if (!Files.exists(path)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Path does not exist: " + dto.getPath());
        }

        Source source = new Source();
        source.setName(dto.getName());
        source.setPath(dto.getPath());
        source.setType(dto.getType() != null ? dto.getType() : "FOLDER");
        source.setEnabled(true);
        source.setScanStatus("IDLE");
        source.setScanIntervalMin(dto.getScanIntervalMin() != null ? dto.getScanIntervalMin() : 60);
        source.setComicCount(0);

        long id = sourceRepository.save(source);
        source.setId(id);
        return source;
    }

    public Source update(long id, Source source) {
        Source existing = findById(id);
        existing.setName(source.getName());
        existing.setPath(source.getPath());
        if (source.getType() != null) {
            existing.setType(source.getType());
        }
        existing.setEnabled(source.isEnabled());
        existing.setScanIntervalMin(source.getScanIntervalMin());
        sourceRepository.update(existing);
        return existing;
    }

    public void delete(long id) {
        findById(id); // validate exists
        // Delete all comics (and their pages) from this source
        List<Comic> comics = comicRepository.findBySourceId(id);
        for (Comic comic : comics) {
            pageRepository.deleteByComicId(comic.getId());
            comicRepository.deleteById(comic.getId());
        }
        sourceRepository.deleteById(id);
    }

    public Source toggleEnabled(long id) {
        Source source = findById(id);
        source.setEnabled(!source.isEnabled());
        sourceRepository.update(source);
        return source;
    }
}
