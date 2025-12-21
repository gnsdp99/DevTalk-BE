# 1. Base Image
FROM eclipse-temurin:21-jre-alpine

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 JAR 파일을 변수로 지정 (Gradle 빌드 결과물 경로)
# 보통 build/libs/프로젝트명-0.0.1-SNAPSHOT.jar 형태로 생성됩니다.
ARG JAR_FILE=build/libs/*.jar

# 4. 호스트의 JAR 파일을 컨테이너 내부로 복사
COPY ${JAR_FILE} app.jar

# 5. 컨테이너 실행 시 작동할 명령어
ENTRYPOINT ["java", "-jar", "/app/app.jar"]