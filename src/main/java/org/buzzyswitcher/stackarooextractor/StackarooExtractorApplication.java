package org.buzzyswitcher.stackarooextractor;

import nu.pattern.OpenCV;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class StackarooExtractorApplication {

    static {
        OpenCV.loadShared(); }

    public static void main(String[] args) {
        SpringApplication.run(StackarooExtractorApplication.class, args);
    }

}
