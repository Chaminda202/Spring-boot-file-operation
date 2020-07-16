package com.spring.fileopertion.service.impl;

import com.spring.fileopertion.config.ApplicationProperties;
import com.spring.fileopertion.exception.FileNotFoundException;
import com.spring.fileopertion.exception.FileStorageException;
import com.spring.fileopertion.model.UploadFileDTO;
import com.spring.fileopertion.model.UploadFileSearchDTO;
import com.spring.fileopertion.model.entity.Document;
import com.spring.fileopertion.repository.DocumentRepository;
import com.spring.fileopertion.service.FileStorageService;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    private static final Logger LOG = LoggerFactory.getLogger(FileStorageServiceImpl.class);
    private final ApplicationProperties applicationProperties;
    private final DocumentRepository documentRepository;

    public FileStorageServiceImpl(ApplicationProperties applicationProperties, DocumentRepository documentRepository) {
        this.applicationProperties = applicationProperties;
        this.documentRepository = documentRepository;
        try {
            Files.createDirectories(Paths.get(this.applicationProperties.getTemporaryDir()).toAbsolutePath().normalize());
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public UploadFileDTO storeFile(MultipartFile file) {
        // Original file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String changedFileName = changeFileName(originalFileName);
        LOG.info("Original file name {} and changed file name {}", originalFileName, changedFileName);
        // Check if the file's name contains invalid characters
        if (originalFileName.contains("..")) {
            throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFileName);
        }
        try {
            Document document = Document.builder()
                    .originalName(originalFileName)
                    .changedName(changedFileName)
                    .extension(StringUtils.getFilenameExtension(originalFileName))
                    .content(file.getBytes())
                    .build();
            this.documentRepository.save(document);
            return UploadFileDTO.builder()
                    .fileName(originalFileName)
                    .extension(StringUtils.getFilenameExtension(originalFileName))
                    .size(file.getSize())
                    .build();
        } catch (IOException ex) {
            LOG.error("Could not store file in data base {}", originalFileName);
            throw new FileStorageException("Could not store file in data base" + originalFileName + ". Please try again!", ex);
        }
    }

    @Override
    public Resource downloadFile(String fileName) {
        Document matchingDoc = this.documentRepository.findFirstByOriginalNameOrderByCreatedDateDesc(fileName);

        if(matchingDoc != null) {
            //Create file
            File createFile = new File(this.applicationProperties.getTemporaryDir() + "\\" + matchingDoc.getChangedName());
            Path path = Paths.get(this.applicationProperties.getTemporaryDir()).toAbsolutePath().resolve(matchingDoc.getChangedName());
            Resource resource = null;
            try(FileOutputStream OutputStream = new FileOutputStream(createFile)) {
                // Starts writing the bytes in it
                OutputStream.write(matchingDoc.getContent());
            }catch (FileNotFoundException e) {
                LOG.error("File not found {}", matchingDoc.getChangedName());
                throw new FileStorageException("File not found " + e.getMessage());
            } catch (IOException ioe) {
                LOG.error("Error is occurred, when writing file");
                throw new FileStorageException("Error is occurred, when writing file " + ioe.getMessage());
            }

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
        } else {
            throw new FileNotFoundException("Could not find the file " + fileName);
        }

    }

    @Override
    public byte[] download(String fileName) {
        Document matchingDoc = this.documentRepository.findFirstByOriginalNameOrderByCreatedDateDesc(fileName);
        return matchingDoc.getContent();
    }

    //delete all temporary files
    @Scheduled(cron="${temporary.files.delete.cron.expression}")
    //@Scheduled(cron="0/30 * * * * *")
    public void deleteAllFiles(){
        LOG.info("Start delete all temporary files");
        try {
            File temporaryFile = new File(this.applicationProperties.getTemporaryDir());
            if(temporaryFile.exists())
                FileUtils.cleanDirectory(temporaryFile);
        } catch (IOException e) {
            LOG.error("Error is occurred, when deleting temporary files {}", e.getMessage());
        }
        LOG.info("End delete all temporary files");
    }

    @Override
    public List<UploadFileDTO> searchFilesByCriteria(UploadFileSearchDTO uploadFileSearchDTO) {
        return this.documentRepository.findAll(DocumentSpecification.searchDocumentBySpec(uploadFileSearchDTO))
                .stream()
                .map(entity -> {
                    return UploadFileDTO.builder()
                            .fileName(entity.getOriginalName())
                            .extension(StringUtils.getFilenameExtension(entity.getOriginalName()))
                            .build();
                }).collect(Collectors.toList());
    }

    private String changeFileName(String originalFileName){
        StringBuilder builder = new StringBuilder();
        builder.append(StringUtils.stripFilenameExtension(originalFileName));
        builder.append("_");
        builder.append(System.currentTimeMillis());
        builder.append(".");
        builder.append(StringUtils.getFilenameExtension(originalFileName));
        return builder.toString();
    }
}
