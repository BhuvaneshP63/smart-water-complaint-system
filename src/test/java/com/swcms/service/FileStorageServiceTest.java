package com.swcms.service;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class FileStorageServiceTest {
    @TempDir Path tempDir;

    @Test
    void storesWorkerFeedbackImageInItsOwnFolder() throws Exception {
        FileStorageService service=new FileStorageService();
        ReflectionTestUtils.setField(service,"uploadDirectory",tempDir.toString());
        byte[] image={(byte)0xff,(byte)0xd8,(byte)0xff,0,0,0,0,0,0,0,0,0};
        MockMultipartFile file=new MockMultipartFile("photo","proof.jpg","image/jpeg",image);
        String stored=service.storeFile(file,"worker-feedback");
        assertTrue(stored.startsWith("worker-feedback/"));
        assertTrue(Files.isRegularFile(tempDir.resolve(stored)));
    }

    @Test
    void rejectsContentThatOnlyClaimsToBeAnImage() {
        FileStorageService service=new FileStorageService();
        ReflectionTestUtils.setField(service,"uploadDirectory",tempDir.toString());
        MockMultipartFile file=new MockMultipartFile("photo","fake.jpg","image/jpeg","not an image".getBytes());
        assertThrows(IllegalArgumentException.class, () -> service.storeFile(file,"feedback"));
    }
}
