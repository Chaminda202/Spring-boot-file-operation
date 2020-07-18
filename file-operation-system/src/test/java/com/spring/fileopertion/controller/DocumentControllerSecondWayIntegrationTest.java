package com.spring.fileopertion.controller;

import com.spring.fileopertion.config.ApplicationProperties;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocumentControllerSecondWayIntegrationTest {
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final String originalFileName = "test.txt";

    @BeforeEach
    public void setUp(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Order(1)
    @DisplayName(value = "Integration test upload single file")
    @Test
    void singleFileUpload() throws Exception {
        //create mock multipart file
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", originalFileName,
                "text/plain", "Integration test for Single file upload".getBytes());

        this.mockMvc.perform(multipart("/files/single/upload")
                .file(mockMultipartFile))
                .andExpect(status().isOk())
                //.andDo(print()) // print the response payload
                .andExpect(jsonPath("$.fileName").value("test.txt"))
                .andExpect(jsonPath("$.extension").value("txt"));
    }

    @Order(2)
    @DisplayName(value = "Integration test download single file")
    @Test
    void downLoadSingleFile() throws Exception {
        String fileName = "test.txt";
        this.mockMvc.perform(get("/files/download/{fileName}", originalFileName))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline;fileName="+ originalFileName))
                .andExpect(content().bytes("Integration test for Single file upload".getBytes()));
    }

    @Order(3)
    @Test
    void tearDown() {
        //remove the uploaded file for integration test
        File file = new File(this.applicationProperties.getUploadDir()+ "\\" + originalFileName);
        if (file.exists()) {
            file.delete();
        }

    }
}