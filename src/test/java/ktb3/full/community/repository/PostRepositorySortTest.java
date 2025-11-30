package ktb3.full.community.repository;

import jakarta.persistence.EntityManager;
import ktb3.full.community.config.JpaTest;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.fixture.PostFixture;
import ktb3.full.community.fixture.UserFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@JpaTest
public class PostRepositorySortTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @MockitoBean
    private DateTimeProvider dateTimeProvider;

    @MockitoSpyBean
    private AuditingHandler auditingHandler;

    @Nested
    class findAllActive {
        @Test
        void 최신순으로_조회한다() {
            // given
            int postCount = 10;
            User user = userRepository.save(UserFixture.createUser());
            LocalDateTime baseTime = LocalDateTime.of(2025, 11, 30, 0, 0, 0);

            auditingHandler.setDateTimeProvider(dateTimeProvider);
            for (int i = 0; i < postCount; i++) {
                given(dateTimeProvider.getNow()).willReturn(Optional.of(baseTime.plusMinutes(i)));
                postRepository.save(PostFixture.createPost(user));
            }
            entityManager.clear();

            // when
            int pageNumber = 0;
            int pageSize = 10;
            Sort createdAtDesc = Sort.by(Sort.Direction.DESC, "createdAt");
            PageRequest request = PageRequest.of(pageNumber, pageSize, createdAtDesc);

            Page<Post> page = postRepository.findAllActive(request);

            // then
            assertThat(page.getNumberOfElements()).isEqualTo(10);
            assertThat(page.getContent())
                    .extracting(Post::getCreatedAt)
                    .isSortedAccordingTo(Comparator.reverseOrder());
        }
    }
}
