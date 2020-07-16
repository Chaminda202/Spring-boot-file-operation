package com.spring.fileopertion.repository;

import com.spring.fileopertion.model.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    Document findFirstByOriginalNameOrderByCreatedDateDesc(String originalName);
}
