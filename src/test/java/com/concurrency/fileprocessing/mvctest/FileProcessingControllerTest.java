package com.concurrency.fileprocessing.mvctest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.concurrency.fileprocessing.controller.FileProcessingController;
import com.concurrency.fileprocessing.processor.MainProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
        MockMultipartFile file1 = new MockMultipartFile("files", "test1.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "data1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "test2.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "data2".getBytes());

        mockMvc.perform(multipart("/api/process-xlsx")
                .file(file1)
                .file(file2))
                .andExpect(status().isOk())
                .andExpect(content().string("All files are being processed asynchronously."));
    }

    @Test
    void testOneEmptyFileAmongMultiple() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("files", "valid.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "valid data".getBytes());
        MockMultipartFile emptyFile = new MockMultipartFile("files", "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        mockMvc.perform(multipart("/api/process-xlsx")
                .file(validFile)
                .file(emptyFile))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void testInvalidFileTypeAmongMultiple() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("files", "valid.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "valid data".getBytes());
        MockMultipartFile invalidFile = new MockMultipartFile("files", "not_csv.txt", "text/plain",
                "invalid".getBytes());

        mockMvc.perform(multipart("/api/process-xlsx")
                .file(validFile)
                .file(invalidFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only .xlsx files are allowed"));
    }

}
