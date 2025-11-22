package ktb3.full.community.fixture;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class MultipartFileFixture {

    public static MultipartFile createImage() {
        return new MockMultipartFile("image", new byte[]{});
    }

    public static MultipartFile createImage(String name) {
        return new MockMultipartFile(name, new byte[]{});
    }
}
