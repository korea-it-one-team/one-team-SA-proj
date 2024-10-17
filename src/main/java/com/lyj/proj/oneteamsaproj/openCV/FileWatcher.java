package com.lyj.proj.oneteamsaproj.openCV;

import java.io.IOException;
import java.nio.file.*;

public class FileWatcher {

    public static boolean watchDirectoryPath(Path path, String fileName, int maxAttempts) {
        // Create the watch service
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

            // Register the directory for create events
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            int attempts = 0; // 감지 시도 횟수

            // Attempt to watch for the file creation
            WatchKey key;
            while ((key = watchService.poll(500, java.util.concurrent.TimeUnit.MILLISECONDS)) != null) { // 0.5초 대기
                attempts++;
                if (attempts > maxAttempts) {
                    System.out.println("파일 생성 감지 시도 횟수를 초과했습니다.");
                    return false; // 지정한 시도 횟수 초과 시 false 반환
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        System.out.println("새로운 파일이 생성되었습니다: " + event.context());
                        // 감지한 파일이 gray_image.jpg인지 확인
                        if (event.context().toString().equals(fileName)) {
                            System.out.println(fileName + " 파일이 생성되었습니다.");
                            return true; // 파일 생성 확인되면 true 반환
                        }
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
