-- Reader Database Schema

CREATE TABLE IF NOT EXISTS `source` (
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`             VARCHAR(255) NOT NULL,
    `path`             VARCHAR(1024) NOT NULL,
    `type`             VARCHAR(20) NOT NULL DEFAULT 'FOLDER' COMMENT 'FOLDER|CBZ|CBR|SEVEN_Z|PDF',
    `enabled`          BOOLEAN DEFAULT TRUE,
    `scan_status`      VARCHAR(20) DEFAULT 'IDLE' COMMENT 'IDLE|SCANNING|ERROR',
    `last_scan_at`     DATETIME,
    `scan_interval_min` INT DEFAULT 60,
    `comic_count`      INT DEFAULT 0,
    `created_at`       DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `comic` (
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `source_id`        BIGINT NOT NULL,
    `title`            VARCHAR(512),
    `author`           VARCHAR(255),
    `publisher`        VARCHAR(255),
    `description`      TEXT,
    `cover_path`       VARCHAR(1024),
    `file_path`        VARCHAR(1024) NOT NULL,
    `file_type`        VARCHAR(20) NOT NULL DEFAULT 'FOLDER' COMMENT 'FOLDER|CBZ|CBR|SEVEN_Z|PDF',
    `page_count`       INT DEFAULT 0,
    `language`         VARCHAR(10),
    `status`           VARCHAR(20) DEFAULT 'ONGOING' COMMENT 'ONGOING|COMPLETED|HIATUS',
    `rating`           DECIMAL(2,1) DEFAULT 0,
    `read_count`       INT DEFAULT 0,
    `last_read_at`     DATETIME,
    `last_read_page`   INT DEFAULT 0,
    `file_size`        BIGINT,
    `file_modified_at` DATETIME,
    `created_at`       DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_source_id` (`source_id`),
    INDEX `idx_title` (`title`(191)),
    INDEX `idx_last_read` (`last_read_at`),
    INDEX `idx_rating` (`rating`),
    UNIQUE KEY `uk_file_path` (`file_path`(500))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `page` (
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `comic_id`         BIGINT NOT NULL,
    `page_number`      INT NOT NULL,
    `file_name`        VARCHAR(255),
    `file_path`        VARCHAR(1024),
    `width`            INT,
    `height`           INT,
    `file_size`        BIGINT,
    INDEX `idx_comic_page` (`comic_id`, `page_number`),
    UNIQUE KEY `uk_comic_page` (`comic_id`, `page_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tag` (
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`             VARCHAR(100) NOT NULL,
    `group_name`       VARCHAR(50) DEFAULT '自定义',
    `color`            VARCHAR(7),
    `parent_id`        BIGINT,
    `comic_count`      INT DEFAULT 0,
    `created_at`       DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `comic_tag` (
    `comic_id`         BIGINT NOT NULL,
    `tag_id`           BIGINT NOT NULL,
    PRIMARY KEY (`comic_id`, `tag_id`),
    INDEX `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `favorite` (
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `comic_id`         BIGINT NOT NULL,
    `group_name`       VARCHAR(50) DEFAULT '默认',
    `created_at`       DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_comic_id` (`comic_id`),
    INDEX `idx_group` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `review` (
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `comic_id`         BIGINT NOT NULL,
    `page_number`      INT COMMENT 'null = comic-level review',
    `content`          TEXT NOT NULL,
    `created_at`       DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_comic_id` (`comic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `reading_history` (
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `comic_id`         BIGINT NOT NULL,
    `start_page`       INT,
    `end_page`         INT,
    `pages_read`       INT,
    `duration_sec`     INT,
    `started_at`       DATETIME DEFAULT CURRENT_TIMESTAMP,
    `ended_at`         DATETIME,
    INDEX `idx_comic_id` (`comic_id`),
    INDEX `idx_started` (`started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `setting` (
    `key`              VARCHAR(100) PRIMARY KEY,
    `value`            TEXT,
    `updated_at`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
