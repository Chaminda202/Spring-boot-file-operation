package com.spring.fileopertion.repository;

import com.spring.fileopertion.model.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Document findFirstByOriginalNameOrderByCreatedDateDesc(String originalName);
}
