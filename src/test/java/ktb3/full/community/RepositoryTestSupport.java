package ktb3.full.community;

import ktb3.full.community.common.config.JpaConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({JpaConfig.class})
@DataJpaTest
public abstract class RepositoryTestSupport {

}
