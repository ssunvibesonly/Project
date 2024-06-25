package image.img_resize.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ImageResizeDto {


    public static class Request {
        @AllArgsConstructor
        @ToString
        @Getter
        @NoArgsConstructor
        @Schema(description = "ImageResizeDto::Request::UploadImage")
        public static class UploadImages {

            private List<MultipartFile> originImages;

        }
    }

    //post 땐 사실 필요업으나 추후에 API 설계 시 리스트로 띄워보기 위한 용도
    public static class Response {
        @ToString
        @AllArgsConstructor
        @Builder
        public static class NewImages {

            private List<MultipartFile> newUploadImages;
            private String resizeWidth;
            private String resizeHeight;
            private String reSize;

            public NewImages getNewImage(List<MultipartFile> newUploadImages, String resizeWidth, String resizeHeight, String reSize) {
                return NewImages.builder()
                        .newUploadImages(newUploadImages)
                        .resizeWidth(resizeWidth)
                        .resizeHeight(resizeHeight)
                        .reSize(reSize)
                        .build();
            }

        }

    }
}
