package com.nisum.util;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtils {

    public static byte[] getFileContentInBytes(String fileName) throws IOException {
        return Files.readAllBytes(new File(fileName).toPath());
    }
    public static String getFileContentDisposition(String fileName){
        return ContentDisposition
                .builder("form-data")
                .name("file")
                .filename(new File(fileName).getName())
                .build().toString();
    }
}
