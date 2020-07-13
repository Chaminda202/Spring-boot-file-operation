package com.spring.fileopertion.service;

import com.spring.fileopertion.model.UploadFileDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    UploadFileDTO storeFile(MultipartFile file);
    Resource downloadFile(String fileName);
}
