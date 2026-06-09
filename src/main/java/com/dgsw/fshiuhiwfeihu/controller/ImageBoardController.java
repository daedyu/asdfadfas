package com.dgsw.fshiuhiwfeihu.controller;

import com.dgsw.fshiuhiwfeihu.entity.ImageBoard;
import com.dgsw.fshiuhiwfeihu.repository.ImageBoardRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;
import java.util.UUID;

@Tag(name = "Image Board", description = "이미지 게시글 API")
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class ImageBoardController {
    private final ImageBoardRepository repository;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Operation(summary = "이미지 게시글 목록 조회")
    @GetMapping("/images")
    public ResponseEntity<List<ImageBoard>> getBoards() {
        return ResponseEntity.ok(repository.findAll());
    }

    @Operation(summary = "이미지 게시글 생성", description = "multipart/form-data로 제목, 내용, 이미지 파일을 업로드합니다.")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageBoard> createBoard(
            @Parameter(description = "게시글 제목", required = true)
            @RequestPart("title") String title,
            @Parameter(description = "게시글 내용", required = true)
            @RequestPart("content") String content,
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestPart("image") MultipartFile image
    ) {
        String originalFileName = image.getOriginalFilename();
        String imageType = image.getContentType();

        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + extension;

        try {
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(imageType)
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
