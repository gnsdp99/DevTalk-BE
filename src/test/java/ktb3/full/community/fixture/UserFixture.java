package ktb3.full.community.fixture;

import ktb3.full.community.domain.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

public class UserFixture {

    public static User createUser(String email, String password, String nickname, String profileImageName, boolean isDeleted) {
        return User.builder()
                .email(email == null ? "email@example.com" : email)
                .password(password == null ? "Password123!" : password)
                .nickname(nickname == null ? "name" : nickname)
                .profileImageName(profileImageName)
                .isDeleted(isDeleted)
                .build();
    }

    public static User createUser(String email, String nickname) {
        return createUser(email, null, nickname, null, false);
    }

    public static User createUser(String email, String nickname, String profileImageName) {
        return createUser(email, null, nickname, profileImageName, false);
    }

    public static User createUser() {
        return createUser(null, null, null, null, false);
    }

    public static User createWithEmail(String email) {
        return createUser(email, null, null, null, false);
    }

    public static User createWithPassword(String password) {
        return createUser(null, password, null, null, false);
    }

    public static User createDeletedWithEmail(String email) {
        return createUser(email, null, null, null, true);
    }

    public static User createWithEmailAndPassword(String email, String password) {
        return createUser(email, password, null, null, false);
    }
}
