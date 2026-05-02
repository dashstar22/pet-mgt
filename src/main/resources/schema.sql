CREATE TABLE IF NOT EXISTS `user` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `username`   VARCHAR(50)  NOT NULL UNIQUE,
  `password`   VARCHAR(255) NOT NULL,
  `email`      VARCHAR(100) DEFAULT NULL,
  `avatar_url` VARCHAR(255) DEFAULT NULL,
  `enabled`    TINYINT      NOT NULL DEFAULT 1,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `role` (
  `id`        BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `role_name` VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS `user_role` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`)
);

CREATE TABLE IF NOT EXISTS `pet_breed` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `breed_name`  VARCHAR(100) NOT NULL,
  `pet_type`    VARCHAR(50)  NOT NULL,
  `description` TEXT         DEFAULT NULL,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `pet` (
  `id`                    BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name`                  VARCHAR(100) NOT NULL,
  `breed_id`              BIGINT       NOT NULL,
  `gender`                VARCHAR(10)  NOT NULL,
  `age`                   INT          NOT NULL,
  `weight`                DECIMAL(5,2) DEFAULT NULL,
  `health_status`         VARCHAR(50)  NOT NULL,
  `vaccine_status`        VARCHAR(50)  DEFAULT NULL,
  `sterilization_status`  VARCHAR(50)  DEFAULT NULL,
  `personality`           TEXT         NOT NULL,
  `adoption_requirement`  TEXT         DEFAULT NULL,
  `status`                VARCHAR(20)  NOT NULL DEFAULT 'available',
  `created_by`            BIGINT       DEFAULT NULL,
  `created_at`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_pet_status`    (`status`),
  INDEX `idx_pet_breed_id`  (`breed_id`)
);

CREATE TABLE IF NOT EXISTS `pet_image` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `pet_id`     BIGINT       NOT NULL,
  `image_url`  VARCHAR(255) NOT NULL,
  `is_cover`   TINYINT      NOT NULL DEFAULT 0,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_image_pet_id` (`pet_id`)
);

CREATE TABLE IF NOT EXISTS `adoption_application` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `pet_id`          BIGINT       NOT NULL,
  `user_id`         BIGINT       NOT NULL,
  `phone`           VARCHAR(20)  NOT NULL,
  `address`         VARCHAR(255) NOT NULL,
  `experience`      TEXT         DEFAULT NULL,
  `accompany_time`  VARCHAR(50)  NOT NULL,
  `reason`          TEXT         NOT NULL,
  `status`          VARCHAR(20)  NOT NULL DEFAULT 'pending',
  `review_comment`  TEXT         DEFAULT NULL,
  `reviewed_by`     BIGINT       DEFAULT NULL,
  `reviewed_at`     DATETIME     DEFAULT NULL,
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_app_status`   (`status`),
  INDEX `idx_app_user_id`  (`user_id`),
  INDEX `idx_app_pet_id`   (`pet_id`)
);

CREATE TABLE IF NOT EXISTS `ai_match_record` (
  `id`                 BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id`            BIGINT   NOT NULL,
  `preference_text`    TEXT     DEFAULT NULL,
  `result_text`        TEXT     DEFAULT NULL,
  `recommended_pet_id` BIGINT   DEFAULT NULL,
  `match_score`        INT      DEFAULT NULL,
  `created_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_match_user_id`  (`user_id`),
  INDEX `idx_match_time`     (`created_at`)
);

CREATE TABLE IF NOT EXISTS `ai_review_record` (
  `id`              BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `application_id`  BIGINT      NOT NULL,
  `result_text`     TEXT        DEFAULT NULL,
  `score`           INT         DEFAULT NULL,
  `suggestion`      VARCHAR(50) DEFAULT NULL,
  `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_review_app_id` (`application_id`)
);
