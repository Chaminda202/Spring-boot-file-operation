package com.spring.fileopertion.controller;

import com.spring.fileopertion.config.ApplicationProperties;
import com.spring.fileopertion.model.UploadFileDTO;
import com.spring.fileopertion.service.FileStorageService;
import com.spring.fileopertion.validator.FileExtensionValidator;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class DocumentControllerTest {
    @Mock
    private FileStorageService fileStorageService;
    @Autowired
    private FileExtensionValidator fileExtensionValidator;
    @Autowired
    private ApplicationProperties applicationProperties;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp(){
        this.mockMvc = MockMvcBuilders.standaloneSetup(
                new DocumentController(fileStorageService, fileExtensionValidator, applicationProperties))
                .build();
    }

    @DisplayName(value = "Unit Test upload single file")
    @Test
    void singleFileUpload() throws Exception {
        //create mock multipart file
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "Single file upload test case".getBytes());

        UploadFileDTO uploadFileDTO = UploadFileDTO.builder()
                .fileName("test.txt")
                .extension("txt")
                .size(1234)
                .build();
        //mock the service call
        when(this.fileStorageService.storeFile(any(MultipartFile.class))).thenReturn(uploadFileDTO);

        this.mockMvc.perform(multipart("/files/single/upload")
                .file(mockMultipartFile))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.fileName").value("test.txt"))
                .andExpect(jsonPath("$.extension").value("txt"))
                .andExpect(jsonPath("$.size").value(1234));

        verify(this.fileStorageService,times(1)).storeFile(any());
    }

    @DisplayName(value = "Unit Test download single file")
    @Test
    void downLoadSingleFile() throws Exception {
        Resource resource = mockResource();

        //mock the service call
        when(this.fileStorageService.downloadFile(any(String.class))).thenReturn(resource);

        this.mockMvc.perform(get("/files/download/{fileName}", "download.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline;fileName="+resource.getFilename()))
                .andExpect(content().bytes("Unit test for single file download".getBytes()));
    }

    private Resource mockResource() {
        return new Resource(){
            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream("Unit test for single file download".getBytes());
            }

            @Override
            public boolean exists() {
                return false;
            }

            @Override
            public URL getURL() throws IOException {
                return null;
            }

            @Override
            public URI getURI() throws IOException {
                return null;
            }

            @Override
            public File getFile() throws IOException {
                File file = new File("download.jpeg");
                System.out.println(file.getAbsolutePath());
                return file;
            }

            @Override
            public long contentLength() throws IOException {
                return 0;
            }

            @Override
            public long lastModified() throws IOException {
                return 0;
            }

            @Override
            public Resource createRelative(String relativePath) throws IOException {
                return null;
            }

            @Override
            public String getFilename() {
                return "download.jpeg";
            }

            @Override
            public String getDescription() {
                return null;
            }
        };
    }
}