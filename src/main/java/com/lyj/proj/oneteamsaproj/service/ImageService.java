package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class ImageService {

    private final ArticleRepository articleRepository;
    @Value("C:\\work_oneteam\\one-team-SA-proj\\src\\main\\resources\\static\\images") // application.yml에서 지정한 경로를 주입
    private String uploadDir; // 이미지 업로드 디렉토리 경로

    public ImageService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public String uploadImage(MultipartFile file, String currentId) throws IOException {
        // 파일을 저장할 디렉토리 경로
        Path directoryPath = Paths.get(uploadDir);
        File directory = directoryPath.toFile();

        // 디렉토리가 없으면 생성
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 파일 이름 생성: 동기화 처리
        synchronized (this) {
            // 파일 이름 생성
            int fileIndex = getNextFileIndex(directoryPath, currentId);
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String fileName = currentId + "-" + fileIndex + fileExtension;

            // 파일 저장 경로
            Path filePath = directoryPath.resolve(fileName);

            // 파일 저장
            file.transferTo(filePath.toFile());

            // 저장된 이미지 파일의 URL 생성
            return "/images/" + fileName; // 반환할 URL 형식
        }
    }


    private int getNextFileIndex(Path directoryPath, String currentId) throws IOException {
        try (Stream<Path> files = Files.list(directoryPath)) {
            return (int) files
                    .filter(path -> path.getFileName().toString().startsWith(currentId + "-"))
                    .count() + 1;
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            return fileName.substring(lastDotIndex); // 확장자 포함
        }
        return ""; // 확장자가 없으면 빈 문자열 반환
    }

    public void saveImage(String imageUrl, int articleId, int boardId) throws IOException {
        // `saveImage` 메서드에서 새 파일 이름 설정을 하지 않음
        // 기존 파일 경로를 그대로 사용
        Path sourcePath = Paths.get(uploadDir, imageUrl.startsWith("/images/") ? imageUrl.substring(8) : imageUrl);

        System.out.println("sourcePath = " + sourcePath);

        if (!Files.exists(sourcePath)) {
            throw new IOException("이미지 파일이 존재하지 않습니다: " + sourcePath);
        }

        // article/boardId 디렉토리 생성
        Path directoryPath = Paths.get(uploadDir, "article", String.valueOf(boardId),String.valueOf(articleId));
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // 파일을 새 디렉토리로 이동
        Path destinationPath = directoryPath.resolve(sourcePath.getFileName());
        Files.move(sourcePath, destinationPath);


    }
}
