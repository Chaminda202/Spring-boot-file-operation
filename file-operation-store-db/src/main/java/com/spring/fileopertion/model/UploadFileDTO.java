package com.spring.fileopertion.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include. NON_NULL)
public class UploadFileDTO {
    private String fileName;
    private String fileDownloadUri;
    private String extension;
    private Long size;
}