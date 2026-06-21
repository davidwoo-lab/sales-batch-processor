# 호스트에서 빌드한 bootJar를 복사해 실행한다.
# (사내 프록시의 SSL 인터셉트 환경에서는 컨테이너 내부 다운로드가 막히므로,
#  네트워크에 자유로운 호스트에서 `./gradlew bootJar`로 빌드한 산출물을 사용한다.)
FROM eclipse-temurin:17-jre
WORKDIR /app

# 사전에 `./gradlew bootJar`로 생성된 실행 가능 jar를 복사
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
