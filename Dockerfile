# 1. JDK 17 기반 이미지 사용
FROM openjdk:17-jdk-slim AS build

# 2. Gradle을 설치하고 프로젝트 디렉터리 설정
WORKDIR /app

# 3. Gradle Wrapper 복사
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# 4. gradlew에 실행 권한 부여
RUN chmod +x gradlew

# 5. CRLF to LF 변환 (Windows와 Linux 간의 줄바꿈 문제 방지)
RUN sed -i 's/\r//' gradlew

# 6. 의존성 다운로드 (최초 빌드 단계)
RUN ./gradlew --no-daemon dependencies

# 7. 프로젝트 소스 파일 복사
COPY src src

# 8. Gradle 빌드 (JAR 파일 생성, 테스트 제외)
RUN ./gradlew build --no-daemon -x test

# 9. 최종 런타임 이미지를 설정 (빌드 결과물만 포함)
FROM openjdk:17-jdk-slim

# 10. 환경 변수 설정 (서버 포트 등)
ENV SERVER_PORT=8088

# 11. JAR 파일 복사
COPY --from=build /app/build/libs/*.jar /app/app.jar

# 12. 포트 노출
EXPOSE 8088

# 13. Spring Boot 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "/app/app.jar"]