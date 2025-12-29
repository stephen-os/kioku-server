package com.kioku.api.repository;

import com.kioku.api.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByUserId(Long userId);

    Optional<Tag> findByUserIdAndName(Long userId, String name);

    boolean existsByUserIdAndName(Long userId, String name);
}