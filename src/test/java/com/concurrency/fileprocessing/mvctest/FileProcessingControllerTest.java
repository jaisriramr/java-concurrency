package com.concurrency.fileprocessing.mvctest;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.concurrency.fileprocessing.controller.FileProcessingController;
import com.concurrency.fileprocessing.processor.MainProcessor;
import com.concurrency.fileprocessing.processor.WorkerProcessor;
import com.concurrency.fileprocessing.queue.Payload;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileProcessingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FileProcessingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MainProcessor mainProcessor;


    @Test
    void testProcessPayload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "sample data".getBytes());
        
        mockMvc.perform(multipart("/api/process-csv")
        .file(file))
        .andExpect(status().isOk())
        .andExpect(content().string("File Processing Started!"));
    }

    @Test
    void testProcessCSV_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/process-csv")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File is empty"));
    }

    @Test
    void testProcessCSV_InvalidFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "sample data".getBytes());

        mockMvc.perform(multipart("/api/process-csv")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only CSV file type is allowed"));
    }
}
