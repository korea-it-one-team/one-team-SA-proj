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

    public String uploadImage(MultipartFile file, String currentId, int piccount) throws IOException {
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
            int fileIndex = getNextFileIndex(directoryPath, currentId, piccount);
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


    private int getNextFileIndex(Path directoryPath, String currentId, int piccount) throws IOException {
        try (Stream<Path> files = Files.list(directoryPath)) {
            // currentId로 시작하는 파일 개수를 계산
            long existingCount = files
                    .filter(path -> path.getFileName().toString().startsWith(currentId + "-"))
                    .count();

            // piccount를 기준으로 다음 파일 인덱스를 반환
            return piccount + (int) existingCount + 1;
        }
    }


    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            return fileName.substring(lastDotIndex); // 확장자 포함
        }
        return ""; // 확장자가 없으면 빈 문자열 반환
    }

    public void saveImage(String imageUrl, int articleId, String boardId) throws IOException {
        // `saveImage` 메서드에서 새 파일 이름 설정을 하지 않음
        // 기존 파일 경로를 그대로 사용
        Path sourcePath = Paths.get(uploadDir, imageUrl.startsWith("/images/") ? imageUrl.substring(8) : imageUrl);


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

    public void deleteImage(int articleId, int boardId) throws IOException {
        Path sourcePath = Paths.get(uploadDir, "article", String.valueOf(boardId), String.valueOf(articleId));
        if (Files.exists(sourcePath)) {
            // 디렉토리 내부 파일 삭제
            try (Stream<Path> files = Files.list(sourcePath)) {
                files.forEach(file -> {
                    try {
                        Files.delete(file); // 파일 삭제
                    } catch (IOException e) {
                        throw new RuntimeException("파일 삭제 중 오류 발생: " + file, e);
                    }
                });
            }
            // 디렉토리 삭제
            Files.delete(sourcePath);
        }
    }
    public void moveImage(String imageUrl, String boardId, int articleId) throws IOException {
        Path dPath = Paths.get(uploadDir, "article", imageUrl.substring(16, 17), String.valueOf(articleId)); // 기존 폴더
        Path sourcePath = Paths.get(uploadDir, "article", boardId, String.valueOf(articleId)); // 새로운 폴더
        // 새로운 폴더 생성 (존재하지 않으면)
        if (!Files.exists(sourcePath)) {
            Files.createDirectories(sourcePath);
        }
        // 기존 폴더의 파일 이동
        if (Files.exists(dPath)) {
            try (Stream<Path> files = Files.list(dPath)) {
                files.forEach(file -> {
                    try {
                        Path targetPath = sourcePath.resolve(file.getFileName()); // 새 위치의 파일 경로
                        Files.move(file, targetPath); // 파일 이동
                    } catch (IOException e) {
                        throw new RuntimeException("파일 이동 중 오류 발생: " + file, e);
                    }
                });
            }
            // 기존 폴더 삭제 (선택)
            Files.deleteIfExists(dPath);
        }
    }

}
