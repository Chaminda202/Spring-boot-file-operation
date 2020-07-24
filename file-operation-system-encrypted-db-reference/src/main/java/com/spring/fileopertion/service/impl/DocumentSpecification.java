package com.spring.fileopertion.service.impl;

import com.spring.fileopertion.model.UploadFileSearchDTO;
import com.spring.fileopertion.model.entity.Document;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class DocumentSpecification {
    private DocumentSpecification(){
    }
    public static Specification<Document> searchDocumentBySpec(UploadFileSearchDTO uploadFileSearchDTO){
        return new Specification<Document>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Document> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if(StringUtils.isEmpty(uploadFileSearchDTO.getFileName())) {
                    predicates.add(criteriaBuilder.equal(root.get("fileName"), uploadFileSearchDTO.getFileName()));
                }
                if(StringUtils.isEmpty(uploadFileSearchDTO.getFileName())) {
                    predicates.add(criteriaBuilder.equal(root.get("extension"), uploadFileSearchDTO.getExtension()));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }

    public static Specification<Document> searchDocumentBySpecTwo(UploadFileSearchDTO uploadFileSearchDTO){
        return new Specification<Document>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Document> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(StringUtils.isEmpty(uploadFileSearchDTO.getFileName())) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("fileName"), uploadFileSearchDTO.getFileName()));
                }
                if(StringUtils.isEmpty(uploadFileSearchDTO.getFileName())) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("extension"), uploadFileSearchDTO.getExtension()));
                }
                return predicate;
            }
        };
    }
}
