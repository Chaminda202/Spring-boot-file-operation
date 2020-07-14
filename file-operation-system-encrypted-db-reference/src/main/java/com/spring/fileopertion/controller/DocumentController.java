package com.spring.fileopertion.controller;

import com.spring.fileopertion.config.ApplicationProperties;
import com.spring.fileopertion.model.UploadFileDTO;
import com.spring.fileopertion.service.FileStorageService;
import com.spring.fileopertion.validator.FileExtensionValidator;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/files")
public class DocumentController {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentController.class);
    private final FileStorageService fileStorageService;
    private final FileExtensionValidator fileExtensionValidator;
    private final ApplicationProperties applicationProperties;

    @PostMapping("/single/upload")
    public ResponseEntity<UploadFileDTO> singleFileUpload(@RequestParam("file") MultipartFile file) throws ValidationException {
        if(this.fileExtensionValidator.validateFileExtension(file)) {
            UploadFileDTO result = this.fileStorageService.storeFile(file);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/files/download/")
                    .path(result.getFileName())
                    .toUriString();
            result.setFileDownloadUri(fileDownloadUri);
            return ResponseEntity.ok().body(result);
        }else {
            throw new ValidationException("Invalid file extension type ");
        }
    }

    @PostMapping("/multiple/upload")
    public ResponseEntity<List<UploadFileDTO>> multipleFilesUpload(@RequestParam("files") MultipartFile[] files) throws ValidationException {

        if(this.fileExtensionValidator.validateFileExtensions(files)) {
            if(files.length > this.applicationProperties.getFileCount()) {
                throw new ValidationException("Exceed max files count " + files.length);
            }else{
                List<UploadFileDTO> response = new ArrayList<>();
                Arrays.asList(files)
                        .stream()
                        .forEach(file -> {
                            UploadFileDTO result = this.fileStorageService.storeFile(file);
                            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                                    .path("/files/download/")
                                    .path(result.getFileName())
                                    .toUriString();
                            result.setFileDownloadUri(fileDownloadUri);
                            response.add(result);
                        });
                return ResponseEntity.ok().body(response);
            }
        }else {
            throw new ValidationException("Invalid file extension types");
        }
    }

    @GetMapping("download/{fileName}")
    public ResponseEntity<Resource> downLoadSingleFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = this.fileStorageService.downloadFile(fileName);
        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            LOG.error("Could not determine file's content type {}", ex.getMessage());
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;fileName="+resource.getFilename()) // when type the url in browser, not display inline. download the file
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline;fileName=" + resource.getFilename())
                .body(resource);
    }

    //Zip file download
    @GetMapping("multiple/download")
    public void downLoadMultipleFiles(@RequestParam("fileName") String[] fileNames, HttpServletResponse response) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            Arrays.asList(fileNames)
                    .stream()
                    .forEach(file -> {
                        // Load file as Resource
                        Resource resource = this.fileStorageService.downloadFile(file);
                        ZipEntry zipEntry = new ZipEntry(resource.getFilename());

                        try {
                            zipEntry.setSize(resource.contentLength());
                            zos.putNextEntry(zipEntry);
                            StreamUtils.copy(resource.getInputStream(), zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            LOG.error("Error in zip file {}", e.getMessage());
                        }
                    });
            zos.finish();
        }
        response.setStatus(200);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;fileName=zipfile");
    }
}
