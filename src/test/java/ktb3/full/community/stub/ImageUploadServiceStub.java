package ktb3.full.community.stub;

import ktb3.full.community.service.ImageUploadService;
import org.springframework.web.multipart.MultipartFile;

public class ImageUploadServiceStub extends ImageUploadService {

    @Override
    public String saveImageAndGetName(MultipartFile image) {
        if (image == null) {
            return null;
        }

        return "/images/" + image.getOriginalFilename();
    }
}
