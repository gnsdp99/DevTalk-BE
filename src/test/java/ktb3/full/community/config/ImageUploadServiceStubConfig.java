package ktb3.full.community.config;

import ktb3.full.community.service.ImageUploadService;
import ktb3.full.community.stub.ImageUploadServiceStub;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ImageUploadServiceStubConfig {

    @Bean
    public ImageUploadService imageUploadService() {
        return new ImageUploadServiceStub();
    }
}