package ktb3.full.community;

import ktb3.full.community.config.ImageUploadServiceStubConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@AutoConfigureMockMvc
@Import({ImageUploadServiceStubConfig.class})
@ActiveProfiles("test")
@SpringBootTest
public abstract class IntegrationTestSupport {

}
