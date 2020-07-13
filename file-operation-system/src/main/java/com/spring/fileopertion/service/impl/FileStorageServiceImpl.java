package com.spring.fileopertion.service.impl;

import com.spring.fileopertion.config.ApplicationProperties;
import com.spring.fileopertion.exception.FileNotFoundException;
import com.spring.fileopertion.exception.FileStorageException;
import com.spring.fileopertion.model.UploadFileDTO;
import com.spring.fileopertion.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    private static final Logger LOG = LoggerFactory.getLogger(FileStorageServiceImpl.class);
    private final ApplicationProperties applicationProperties;
    private final Path fileStoragePath;

    public FileStorageServiceImpl(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.fileStoragePath = Paths.get(this.applicationProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStoragePath);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public UploadFileDTO storeFile(MultipartFile file) {
        // Original file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        LOG.info("Original file name {}", originalFileName);
        // Check if the file's name contains invalid characters
        if (originalFileName.contains("..")) {
            throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFileName);
        }
        Path filePath  = Paths.get(this.applicationProperties.getUploadDir() + "\\" + originalFileName).toAbsolutePath().normalize();
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return UploadFileDTO.builder()
                    .fileName(originalFileName)
                    .extension(StringUtils.getFilenameExtension(originalFileName))
                    .size(file.getSize())
                    .build();
        } catch (IOException ex) {
            LOG.error("Could not store file ", originalFileName);
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    @Override
    public Resource downloadFile(String fileName) {
        Path path = Paths.get(this.applicationProperties.getUploadDir()).toAbsolutePath().resolve(fileName);

        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
            if(resource.exists() && resource.isReadable()){
                return resource;
            }else{
                throw new FileStorageException("File doesn't exist or not readable");
            }
        } catch (MalformedURLException e) {
            LOG.error("Could not read file {}", fileName);
            throw new FileNotFoundException("Could not read file " + fileName, e);
        }
    }
}
