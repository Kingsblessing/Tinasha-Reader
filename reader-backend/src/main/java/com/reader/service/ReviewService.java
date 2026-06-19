package com.reader.service;

import com.reader.common.BusinessException;
import com.reader.common.ErrorCode;
import com.reader.model.dto.ReviewCreateDTO;
import com.reader.model.entity.Review;
import com.reader.repository.ComicRepository;
import com.reader.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ComicRepository comicRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ComicRepository comicRepository) {
        this.reviewRepository = reviewRepository;
        this.comicRepository = comicRepository;
    }

    public List<Review> findByComicId(long comicId) {
        return reviewRepository.findByComicId(comicId);
    }

    public Review create(ReviewCreateDTO dto) {
        // Validate comic exists
        if (comicRepository.findById(dto.getComicId()) == null) {
            throw new BusinessException(ErrorCode.COMIC_NOT_FOUND, "Comic not found: " + dto.getComicId());
        }

        Review review = new Review();
        review.setComicId(dto.getComicId());
        review.setPageNumber(dto.getPageNumber());
        review.setContent(dto.getContent());

        long id = reviewRepository.save(review);
        review.setId(id);
        return review;
    }

    public Review update(long id, String content) {
        Review existing = reviewRepository.findById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Review not found: " + id);
        }
        existing.setContent(content);
        reviewRepository.update(existing);
        return existing;
    }

    public void delete(long id) {
        Review existing = reviewRepository.findById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Review not found: " + id);
        }
        reviewRepository.deleteById(id);
    }
}
