package image.img_resize.service;

import image.img_resize.dto.ImageResizeDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ImageResizeService {

    public List<MultipartFile> postNewImage(List<MultipartFile> originImages){

        List<MultipartFile> resizedImages = new ArrayList<>(); // 리사이징된 이미지 반환을 위한 List

        for (MultipartFile originImage : originImages) {

            String originName = originImage.getOriginalFilename(); // 원본 이미지명
            String origin = originImage.getContentType(); // 확장자명

            try {
                BufferedImage originFile = ImageIO.read(originImage.getInputStream());

                int originWidth = originFile.getWidth(); // 원본 이미지 width
                int originHeight = originFile.getHeight(); // 원본 이미지 height

                BufferedImage resizedImage = null;
                if (originWidth > 1200) {

                    int resizeHeight = (originHeight * 1200) / originWidth; // 리사이징 height

                    // Graphics2D를 이용한 이미지 리사이징
                    resizedImage = new BufferedImage(1200, resizeHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics2D = resizedImage.createGraphics();
                    graphics2D.drawImage(originFile, 0, 0, 1200, resizeHeight, null);
                    graphics2D.dispose();

                    // BufferedImage를 MultipartFile로 변환하여 리스트에 추가
                    MultipartFile resizeMultiPartFile = convertBufferedImageToMultipartFile(resizedImage, originName);
                    resizedImages.add(resizeMultiPartFile);

                    log.info("width가 1200초과 인 경우 // fileName: " + originName + " re-width: " + String.valueOf(resizedImage.getWidth()) + " re-height: " + String.valueOf(resizedImage.getHeight()));
                }
                if (originWidth <= 1200) {

                    resizedImages.add(originImage);
                   //log.info("width가 1200이하 인 경우 fileName: " + originName + " re-width: " + String.valueOf(originImage.g) + " re-height: " + String.valueOf(resizedImage.getHeight()));
                }

            } catch (IOException e) {
                log.info(originImage.getOriginalFilename());
                e.printStackTrace();
            }
        }

        return resizedImages;
    }

    private MultipartFile convertBufferedImageToMultipartFile(BufferedImage image, String originalFilename) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (originalFilename.toLowerCase().endsWith(".jpg") || originalFilename.toLowerCase().endsWith(".jpeg")){
            ImageIO.write(image,"jpg",baos);
        }
        if (originalFilename.toLowerCase().endsWith(".png")){
            ImageIO.write(image,"png",baos);
        }
        byte[] imageBytes = baos.toByteArray();
        return new CustomMultipartFile(imageBytes, originalFilename);
    }

    @AllArgsConstructor
    public static class CustomMultipartFile implements MultipartFile {

        private final byte[] fileContent;
        private final String fileName;

        /*public CustomMultipartFile(byte[] fileContent, String fileName) {
            this.fileContent = fileContent;
            this.fileName = fileName;
        }*/

        @Override
        public String getName() {
            return fileName;
        }

        @Override
        public String getOriginalFilename() {
            return fileName;
        }

        @Override
        public String getContentType() {
            return "image/jpeg";
        }

        @Override
        public boolean isEmpty() {
            return fileContent.length == 0;
        }

        @Override
        public long getSize() {
            return fileContent.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return fileContent;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(fileContent);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            Files.write(dest.toPath(), fileContent);
        }
    }
}
