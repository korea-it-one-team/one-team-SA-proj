package com.lyj.proj.oneteamsaproj.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.lyj.proj.oneteamsaproj.repository.AmdGifticonRepository;
import com.lyj.proj.oneteamsaproj.repository.GifticonRepository;
import com.lyj.proj.oneteamsaproj.vo.Gifticon;
import com.lyj.proj.oneteamsaproj.vo.Gifticon_Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class AdmGifticonService {

    @Autowired
    private AmdGifticonRepository admgifticonRepository;

    @Autowired
    private GifticonRepository gifticonRepository;

    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String keyFileLocation;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    public void updateStockAndImage(int gifticonId,  MultipartFile file) throws IOException {

        String uuid = UUID.randomUUID().toString() + ".jpg";
        String ext = "image/jpeg"; // 강제로 JPG로 설정

        InputStream keyFile = ResourceUtils.getURL(keyFileLocation).openStream();

        Storage storage = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(keyFile))
                .build()
                .getService();


        // Google Cloud Storage에 이미지 업로드
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, uuid)
                .setContentType(ext) // MIME 타입 설정
                .build();
        Blob blob = storage.create(blobInfo, file.getBytes());
        System.out.println("asdf : " + bucketName + "/" + uuid);

        admgifticonRepository.gifticonStockSave(gifticonId,bucketName + "/" + uuid);
        int gifticonStockCount = admgifticonRepository.gifticonStockCount(gifticonId);
        gifticonRepository.stockAdd(gifticonId,gifticonStockCount);
    }
}
