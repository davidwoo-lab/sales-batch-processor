# ===== 1단계: 빌드 (JDK 17로 bootJar 생성) =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Gradle wrapper와 빌드 스크립트를 먼저 복사해 의존성 캐시 레이어를 활용한다
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# 소스 복사 후 테스트는 제외하고 실행 가능한 jar 생성
COPY src ./src
RUN ./gradlew clean bootJar -x test --no-daemon

# ===== 2단계: 실행 (가벼운 JRE 이미지) =====
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드 단계에서 생성된 jar만 가져온다
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
