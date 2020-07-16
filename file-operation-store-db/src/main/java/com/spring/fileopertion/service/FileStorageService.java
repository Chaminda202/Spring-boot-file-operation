package com.spring.fileopertion.service;

import com.spring.fileopertion.model.UploadFileDTO;
import com.spring.fileopertion.model.UploadFileSearchDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {
    UploadFileDTO storeFile(MultipartFile file);
    Resource downloadFile(String fileName);
    byte[] download(String fileName);
    List<UploadFileDTO> searchFilesByCriteria(UploadFileSearchDTO uploadFileSearchDTO);
}
