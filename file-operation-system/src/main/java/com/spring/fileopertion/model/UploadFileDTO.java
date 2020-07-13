package com.spring.fileopertion.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadFileDTO {
    private String fileName;
    private String fileDownloadUri;
    private String extension;
    private long size;
}