package image.img_resize.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ImageResizeService {

    public List<MultipartFile> postNewImage(List<MultipartFile> originImages) throws IOException, ImageProcessingException, MetadataException {

        List<MultipartFile> resizedImages = new ArrayList<>(); // 리사이징된 이미지 반환을 위한 List

        Metadata metadata;
        ExifIFD0Directory directory;

        int orientation = 1;

        for (MultipartFile originImage : originImages) {

            String originName = originImage.getOriginalFilename(); // 원본 이미지명


            try {
                BufferedImage originFile = ImageIO.read(originImage.getInputStream());

                int originWidth = originFile.getWidth(); // 원본 이미지 width
                int originHeight = originFile.getHeight(); // 원본 이미지 height
                int resizeHeight = (originHeight * 1200) / originWidth; // 리사이징 height

                int resizeWidth = (originWidth * 1200) / originHeight; // 오리엔테이션이 6인 경우 width와 height가 바뀌어 나오는 것 때문에 작성

                metadata = ImageMetadataReader.readMetadata(originImage.getInputStream()); //멀티파트 파일 Metadata 읽어오기

                assert metadata != null;
                directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

                log.info(directory == null ? "null" : "null아님");

                /**
                 * 안드로이드 폰으로 촬영한 경우 회전되어 이미지가 저장되는 경우가 있어 Exif값 읽어오기(orientation)
                 * */
                if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {

                    orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);

                    if (orientation == 1 && originWidth > 1200) {

                            BufferedImage resizedImage = Thumbnails.of(originFile)
                                    .size(1200, resizeHeight)
                                    .asBufferedImage();

                            assert originName != null; //assert -> 조건을 확인하고 true이면 다음 라인으로, false라면 AssertError 발생
                            MultipartFile resizeMultiPartFile = convertBufferedImageToMultipartFile(resizedImage, originName);

                            resizedImages.add(resizeMultiPartFile);

                    }

                    if (orientation != 1 && originWidth > 1200) {

                            BufferedImage resizedImage = Thumbnails.of(originFile)
                                    .size(originWidth, originHeight)
                                    .asBufferedImage();


                            switch (orientation) {
                                case 3:
                                    resizedImage = Thumbnails.of(originFile).rotate(180).size(1200, resizeHeight).asBufferedImage();
                                    break;
                                case 6:
                                    resizedImage = Thumbnails.of(originFile).rotate(90).size(resizeWidth, 1200).asBufferedImage();
                                    break;
                                case 8:
                                    resizedImage = Thumbnails.of(originFile).rotate(-90).size(resizeWidth, 1200).asBufferedImage();
                                    break;
                            }
                            assert originName != null;
                            MultipartFile resizeMultiPartFile = convertBufferedImageToMultipartFile(resizedImage, originName);

                            resizedImages.add(resizeMultiPartFile);


                        }
                    }else {

                    assert originName != null;
                    BufferedImage resizedImage = Thumbnails.of(originFile).size(1200,resizeHeight).asBufferedImage();

                    MultipartFile resizeMultiPartFile = convertBufferedImageToMultipartFile(resizedImage,originName);
                    resizedImages.add(resizeMultiPartFile);
                }

                // Graphics2D를 이용한 이미지 리사이징 + 품질 컨트롤 -> Thumnailator보다 품질이 떨어짐(열화 현상)
                    /*resizedImage = new BufferedImage(1200, resizeHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics2D = resizedImage.createGraphics();

                    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                    graphics2D.drawImage(originFile, 0, 0, 1200, resizeHeight, null);
                    graphics2D.dispose();
*/
                // BufferedImage를 MultipartFile로 변환하여 리스트에 추가

            } catch (IOException e) {
                log.info(originImage.getOriginalFilename());
                e.printStackTrace();
            }
        }

        return resizedImages;
    }


    private MultipartFile convertBufferedImageToMultipartFile(BufferedImage image, String originalFilename) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (originalFilename.toLowerCase().endsWith(".jpg") || originalFilename.toLowerCase().endsWith(".jpeg")) {
            ImageIO.write(image, "jpg", baos);
        }
        if (originalFilename.toLowerCase().endsWith(".png")) {
            ImageIO.write(image, "png", baos);
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
        @NonNull
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
