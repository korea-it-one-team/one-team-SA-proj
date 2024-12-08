# 1. JDK 17 기반 이미지 사용
FROM openjdk:17-jdk-slim AS build

# 2. Gradle을 설치하고 프로젝트 디렉터리 설정
WORKDIR /app

# 3. Gradle Wrapper 복사
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# 4. 필요한 도구 및 패키지 설치
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    unzip \
    curl \
    libx11-6 \
    libxcomposite1 \
    libxrandr2 \
    libxdamage1 \
    libxss1 \
    libgdk-pixbuf2.0-0 \
    x11-apps \
    ca-certificates \
    xvfb \
    && rm -rf /var/lib/apt/lists/*

# 5. Google Chrome 설치
RUN wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
    && apt-get update \
    && apt-get install -y ./google-chrome-stable_current_amd64.deb \
    && rm google-chrome-stable_current_amd64.deb

# 6. 크롬 드라이버 다운로드
RUN wget -q "https://storage.googleapis.com/chrome-for-testing-public/131.0.6778.87/linux64/chromedriver-linux64.zip" -O /tmp/chromedriver-linux64.zip \
    && unzip /tmp/chromedriver-linux64.zip -d /usr/local/bin/ \
    && rm /tmp/chromedriver-linux64.zip

# 7. Xvfb 실행 및 DISPLAY 환경 변수 설정
RUN Xvfb :99 -screen 0 1024x768x24 & export DISPLAY=:99

# 8. gradlew에 실행 권한 부여
RUN chmod +x gradlew

# 9. CRLF to LF 변환
RUN sed -i 's/\r//' gradlew

# 10. 의존성 다운로드 (최초 빌드 단계)
RUN ./gradlew --no-daemon dependencies

# 11. 프로젝트 소스 파일 복사
COPY src src

# 12. Gradle 빌드 (JAR 파일 생성, 테스트 제외)
RUN ./gradlew build --no-daemon -x test

# 13. 최종 런타임 이미지를 설정 (빌드 결과물만 포함)
FROM openjdk:17-jdk-slim

# 14. 환경 변수 설정
ENV SERVER_PORT=8088

# 15. WAR 파일 복사
COPY --from=build /app/build/libs/*.war /app/app.war

# 16. HTML, CSS, JS, Thymeleaf 템플릿 파일을 웹 서버에 복사 (빌드된 static과 templates)
COPY src/main/resources/static /app/static
COPY src/main/resources/templates /app/templates

# 17. application.yml 파일 복사
COPY src/main/resources/application.yml /app/application.yml

# 18. 포트 노출
EXPOSE 8088

# 19. Spring Boot 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "/app/app.war"]
