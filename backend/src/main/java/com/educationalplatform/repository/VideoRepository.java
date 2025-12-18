package com.educationalplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.educationalplatform.domain.model.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
}