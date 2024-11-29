package com.lyj.proj.oneteamsaproj.controller;


import com.lyj.proj.oneteamsaproj.service.ImageService;
import com.lyj.proj.oneteamsaproj.vo.ImageUploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file,
                                                           @RequestParam("currentId") String currentId,
                                                           @RequestParam(defaultValue = "0") int piccount) {
        try {
            // 이미지 업로드 처리
            String imageUrl = imageService.uploadImage(file, currentId, piccount);
            ImageUploadResponse response = new ImageUploadResponse(imageUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ImageUploadResponse("Upload failed"));
        }
    }
}
