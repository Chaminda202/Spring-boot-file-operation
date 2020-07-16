package com.spring.fileopertion.service.impl;

import com.spring.fileopertion.config.ApplicationProperties;
import com.spring.fileopertion.exception.FileNotFoundException;
import com.spring.fileopertion.exception.FileStorageException;
import com.spring.fileopertion.model.UploadFileDTO;
import com.spring.fileopertion.model.UploadFileSearchDTO;
import com.spring.fileopertion.model.entity.Document;
import com.spring.fileopertion.repository.DocumentRepository;
import com.spring.fileopertion.service.EncryptAndDecryptService;
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

import javax.crypto.Cipher;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    private static final Logger LOG = LoggerFactory.getLogger(FileStorageServiceImpl.class);
    private final ApplicationProperties applicationProperties;
    private final EncryptAndDecryptService encryptAndDecryptService;
    private final DocumentRepository documentRepository;
    // private final Path fileStoragePath;

    public FileStorageServiceImpl(ApplicationProperties applicationProperties, EncryptAndDecryptService encryptAndDecryptService,
                                  DocumentRepository documentRepository) {
        this.applicationProperties = applicationProperties;
        this.encryptAndDecryptService = encryptAndDecryptService;
        this.documentRepository = documentRepository;
        // this.fileStoragePath = Paths.get(this.applicationProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(Paths.get(this.applicationProperties.getUploadDir()).toAbsolutePath().normalize());
            Files.createDirectories(Paths.get(this.applicationProperties.getTemporyDir()).toAbsolutePath().normalize());
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
        Path filePath  = Paths.get(this.applicationProperties.getTemporyDir() + "\\" + originalFileName).toAbsolutePath().normalize();

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            //Created encrypted file
            File encryptedFile = new File(this.applicationProperties.getUploadDir() + "\\" + changedFileName);
            this.encryptAndDecryptService.fileProcessor(Cipher.ENCRYPT_MODE,
                    filePath.toFile(), encryptedFile);
            //delete the original file from the location
            deleteOriginalFile(originalFileName);
            Document document = Document.builder()
                    .originalName(originalFileName)
                    .changedName(changedFileName)
                    .extension(StringUtils.getFilenameExtension(originalFileName))
                    .build();
            this.documentRepository.save(document);
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
        Document matchingDoc = this.documentRepository.findFirstByOriginalNameOrderByCreatedDateDesc(fileName);

        Resource resource;
        try {
            Path path = Paths.get(this.applicationProperties.getUploadDir()).toAbsolutePath().resolve(matchingDoc.getChangedName());
            //Created decrypted file
            File decryptedFile = new File(this.applicationProperties.getTemporyDir() + "\\" + matchingDoc.getChangedName().replace("encrypted", matchingDoc.getExtension()));
            this.encryptAndDecryptService.fileProcessor(Cipher.DECRYPT_MODE,
                    path.toFile(), decryptedFile);
            URI docUrl = new URI(decryptedFile.toURI().toString());
            resource = new UrlResource(docUrl);
            if(resource.exists() && resource.isReadable()){
                return resource;
            }else{
                throw new FileStorageException("File doesn't exist or not readable");
            }
        } catch (MalformedURLException | URISyntaxException e) {
            LOG.error("Could not read file {}", fileName);
            throw new FileNotFoundException("Could not read file " + fileName, e);
        }
    }

    //delete all temporary files
    @Scheduled(cron="${temporary.files.delete.cron.expression}")
    //@Scheduled(cron="0/30 * * * * *")
    public void deleteAllFiles(){
        LOG.info("Start delete all temporary files");
        try {
            File temporaryFile = new File(this.applicationProperties.getTemporyDir());
            if(temporaryFile.exists())
                FileUtils.cleanDirectory(temporaryFile);
        } catch (IOException e) {
            e.printStackTrace();
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
        builder.append(new Long(System.currentTimeMillis()).toString());
        builder.append(".");
        builder.append("encrypted");
        return builder.toString();
    }

    private void deleteOriginalFile(String originalFileName) {
        File file = new File(this.applicationProperties.getTemporyDir() + "\\" + originalFileName);
        if (file.exists()) {
            file.delete();
        }
    }
}
