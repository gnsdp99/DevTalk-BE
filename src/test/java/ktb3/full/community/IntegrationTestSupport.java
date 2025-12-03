package ktb3.full.community;

import ktb3.full.community.config.ImageUploadServiceStubConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"classpath:sql/truncate.sql"})
@AutoConfigureMockMvc
@Import({ImageUploadServiceStubConfig.class})
@ActiveProfiles("test")
@SpringBootTest
public abstract class IntegrationTestSupport {

}
