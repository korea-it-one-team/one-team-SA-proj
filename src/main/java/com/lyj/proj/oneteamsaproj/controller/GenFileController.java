package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.exception.GenFileNotFoundException;
import com.lyj.proj.oneteamsaproj.service.GenFileService;
import com.lyj.proj.oneteamsaproj.vo.GenFile;
import com.lyj.proj.oneteamsaproj.vo.ResultData;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

@Controller
public class GenFileController {

    @Value("${custom.genFileDirPath}")
    private String genFileDirPath; // 이미지 저장 경로

    @Value("${custom.videoFileDirPath}")
    private String videoFileDirPath; // 동영상 저장 경로

    @Autowired
    private GenFileService genFileService;

    // 파일 업로드 처리
    @RequestMapping("/common/genFile/doUpload")
    @ResponseBody
    public ResultData doUpload(@RequestParam Map<String, Object> param, MultipartRequest multipartRequest) {
        MultipartFile file = multipartRequest.getFile("file"); // 파일 받아오기
        String fileType = file.getContentType();  // 파일의 MIME 타입 확인

        // 이미지와 동영상을 구분하여 저장 경로 설정
        String saveDir;
        if (fileType.startsWith("image")) {
            saveDir = genFileDirPath;  // 이미지 저장 경로
        } else if (fileType.startsWith("video")) {
            saveDir = videoFileDirPath;  // 동영상 저장 경로
        } else {
            // 다른 파일은 기본적으로 이미지 폴더에 저장하거나 다른 처리를 할 수 있음
            saveDir = genFileDirPath;  // 기본 저장 경로
        }

        // 파일 저장 처리
        File targetFile = new File(saveDir, file.getOriginalFilename());
        try {
            file.transferTo(targetFile);  // 파일을 지정한 경로에 저장
        } catch (IOException e) {
            // 업로드 실패 시
            return ResultData.from("F-1", "파일 업로드 실패");
        }

        // 업로드 성공 시
        return ResultData.from("S-1", "파일 업로드 성공", "fileName", file.getOriginalFilename());
    }

    // 파일 다운로드 처리
    @GetMapping("/common/genFile/doDownload")
    public ResponseEntity<Resource> downloadFile(int id, HttpServletRequest request) throws IOException {
        GenFile genFile = genFileService.getGenFile(id);

        if (genFile == null) {
            throw new GenFileNotFoundException();
        }

        String filePath = genFile.getFilePath(genFileDirPath);

        Resource resource = new InputStreamResource(new FileInputStream(filePath));

        // 파일의 MIME 타입 확인
        String contentType = request.getServletContext().getMimeType(new File(filePath).getAbsolutePath());

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + genFile.getOriginFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType)).body(resource);
    }

    // 파일을 직접 보여주기 위한 처리 (이미지 또는 동영상)
    @GetMapping("/common/genFile/file/{relTypeCode}/{relId}/{typeCode}/{type2Code}/{fileNo}")
    public ResponseEntity<Resource> showFile(HttpServletRequest request, @PathVariable String relTypeCode,
                                             @PathVariable int relId, @PathVariable String typeCode, @PathVariable String type2Code,
                                             @PathVariable int fileNo) throws FileNotFoundException {
        GenFile genFile = genFileService.getGenFile(relTypeCode, relId, typeCode, type2Code, fileNo);

        if (genFile == null) {
            throw new GenFileNotFoundException();
        }

        String contentType = request.getServletContext().getMimeType(genFile.getOriginFileName());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // 경로 선택: 동영상과 일반 파일 구분
        String filePath;
        if (contentType.startsWith("video")) {
            filePath = genFile.getFilePath(videoFileDirPath); // 동영상 저장 경로
        } else {
            filePath = genFile.getFilePath(genFileDirPath);  // 이미지 저장 경로
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        Resource resource = new InputStreamResource(new FileInputStream(file));

        // 동영상은 스트리밍 처리
        if (contentType.startsWith("video")) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + genFile.getOriginFileName() + "\"")
                    .body(resource);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + genFile.getOriginFileName() + "\"")
                .body(resource);
    }
}