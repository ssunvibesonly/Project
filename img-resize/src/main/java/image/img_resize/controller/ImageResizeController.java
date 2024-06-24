package image.img_resize.controller;

import image.img_resize.dto.ImageResizeDto;
import image.img_resize.service.ImageResizeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
@Tag(name = "이미지 리사이징 컨트롤러")
public class ImageResizeController {

    private final ImageResizeService service;

    @PostMapping(value = "/image-resize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Post Images", description = "이미지 리사이징을 위해 원본 이미지 업로드")
    public ResponseEntity<ImageResizeDto.Response.NewImages> postOriginImage(
            @Valid @Parameter(required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "originImages", required = false) List<MultipartFile> originalImages) {

        //원본 이미지 받아오는지 확인용
        for (MultipartFile originImage : originalImages) {
            log.info("Received file: {}", originImage.getOriginalFilename());
        }

        //리사이징된 이미지 리스트 반환
        List<MultipartFile> resizeImages = service.postNewImage(originalImages);

        //반환된 리스트 폴더에 업로드
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayUpload = localDate.format(formatter);

        String parentPath = "D:\\Lifezip\\img-resize_sy";
        String childPath = "D:\\Lifezip\\img-resize_sy\\" + todayUpload;

        File file = new File(parentPath, childPath);

        // 오늘 날짜 디렉토리 경로 설정
        Path path = Paths.get(childPath);

        try {

            //디렉토리가 없으면 오늘 날짜로 디렉토리 생성하고 이미지 추가
            if (!Files.exists(path)) {
                //오늘 날짜에 해당하는 dir 생성
                Files.createDirectory(path);
                log.info("새로운 디렉토리가 생성 되었습니다. 경로 : " + path);

                for (MultipartFile resizeImage : resizeImages) {

                    File uploadFile = new File(childPath, resizeImage.getName());
                    resizeImage.transferTo(uploadFile);

                    log.info("업로드 된 파일 리스트 : " + uploadFile);
                }

            }

            //디렉토리가 이미 생성되어 있고, 폴더명이 오늘 날짜와 같다면 이미지만 추가
            if (Files.exists(path)) {
                log.info("이미 디렉토리가 있습니다.");
                for (MultipartFile resizeImage : resizeImages) {

                    File uploadFile = new File(childPath, resizeImage.getName());
                    resizeImage.transferTo(uploadFile);

                    log.info("업로드 된 파일 리스트 : " + uploadFile);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        //new ImageResizeDto.Response.NewImages(resizeImage)
        return null;
    }
}
