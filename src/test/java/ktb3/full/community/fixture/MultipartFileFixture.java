package ktb3.full.community.fixture;

import org.springframework.mock.web.MockMultipartFile;

public class MultipartFileFixture {

    public static MockMultipartFile createImage() {
        return new MockMultipartFile("image", new byte[]{});
    }

    public static MockMultipartFile createProfileImage() {
        return new MockMultipartFile("profileImage", new byte[]{});
    }

    public static MockMultipartFile createImage(String name) {
        return new MockMultipartFile(name, new byte[]{});
    }
}
