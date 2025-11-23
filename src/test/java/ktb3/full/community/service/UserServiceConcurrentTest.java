package ktb3.full.community.service;

import ktb3.full.community.config.IntegrationTest;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.dto.request.UserAccountUpdateRequest;
import ktb3.full.community.dto.request.UserRegisterRequest;
import ktb3.full.community.fixture.MultipartFileFixture;
import ktb3.full.community.fixture.UserFixture;
import ktb3.full.community.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class UserServiceConcurrentTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    @Nested
    class register {

        @RepeatedTest(value = 5)
        void 동시에_동일한_이메일로_가입하면_한_명만_성공한다() {
            // given
            int NUM_THREAD = 3;

            // when
            try (ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD)) {
                for (int i = 0; i < NUM_THREAD; i++) {
                    UserRegisterRequest request = new UserRegisterRequest("dupEmail@example.com", "Password123!", "name" + i, MultipartFileFixture.createProfileImage("name" + i));
                    executor.submit(() -> userService.register(request));
                }
            }

            // then
            assertThat(userRepository.findAll().stream()
                    .filter(user -> "dupEmail@example.com".equals(user.getEmail()))
                    .count()).isEqualTo(1);
        }

        @RepeatedTest(value = 5)
        void 동시에_동일한_닉네임으로_가입하면_한_명만_성공한다() {
            // given
            int NUM_THREAD = 3;

            // when
            try (ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD)) {
                for (int i = 0; i < NUM_THREAD; i++) {
                    UserRegisterRequest request = new UserRegisterRequest("email + " + i + "@example.com", "Password123!", "dupName", MultipartFileFixture.createProfileImage("name" + i));
                    executor.submit(() -> userService.register(request));
                }
            }

            // then
            assertThat(userRepository.findAll().stream()
                    .filter(user -> "dupName".equals(user.getNickname()))
                    .count()).isEqualTo(1);

            System.out.println("테스트 종료");
        }
    }

    @Nested
    class updateAccount {

        @RepeatedTest(value = 5)
        void 동시에_동일한_닉네임으로_변경하면_한_명만_성공한다() {
            // given
            int NUM_THREAD = 3;
            List<Long> userIds = new ArrayList<>();

            for (int i = 1; i <= NUM_THREAD; i++) {
                User user = userRepository.save(UserFixture.createWithUnique("email" + i + "@example.com", "name" + i, "profileImageName" + i));
                userIds.add(user.getId());
            }

            // when
            UserAccountUpdateRequest request = new UserAccountUpdateRequest("dupName", null);

            try (ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD)) {
                for (int i = 0; i < NUM_THREAD; i++) {
                    int finalI = i;
                    executor.submit(() -> userService.updateAccount(userIds.get(finalI), request));
                }
            }

            // then
            assertThat(userRepository.findAll().stream()
                    .filter(user -> "dupName".equals(user.getNickname()))
                    .count()).isEqualTo(1);
        }
    }
}
