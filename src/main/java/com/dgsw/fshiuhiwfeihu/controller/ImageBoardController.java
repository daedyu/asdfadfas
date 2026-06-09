package com.dgsw.fshiuhiwfeihu.controller;

import com.dgsw.fshiuhiwfeihu.entity.ImageBoard;
import com.dgsw.fshiuhiwfeihu.repository.ImageBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
class ImageBoardController {
    private final ImageBoardRepository repository;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private static String bucket;

    @GetMapping("/images")
    public ResponseEntity<List<ImageBoard>> getBoards() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping("/images")
    public ResponseEntity<ImageBoard> createBoard(
            @RequestPart("title") String title,
            @RequestPart("content") String content,
            @RequestPart("image") MultipartFile image
    ) {
        String originalFileName = image.getOriginalFilename();
        String imageType = image.getContentType();

        String fileName = originalFileName.substring(0, originalFileName.lastIndexOf(".")) + "." + imageType;

        try {
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .build(),
                    RequestBody.fromInputStream(image.getInputStream(), image.getSize())
            );
            ImageBoard board = repository.save(new ImageBoard(title, content, s3Client.utilities().getUrl(
                    GetUrlRequest.builder()
                            .bucket(bucket)
                            .key(fileName)
                            .build()).toString()
            ));
            return ResponseEntity.ok(board);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
