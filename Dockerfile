# 1. Builder 단계: JAR 파일에서 레이어를 추출(Extract)합니다.

# "builder"라는 이름으로 임시 작업 공간을 만듭니다.
FROM eclipse-temurin:21-jre-alpine AS builder

# 작업 폴더를 만듭니다.
WORKDIR /builder

# 빌드된 뚱뚱한 JAR 파일을 가져옵니다.
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# JAR를 4가지 부품으로 분해(extract)합니다. (Spring Boot가 제공하는 layertools 사용)
RUN java -Djarmode=layertools -jar app.jar extract

# Runner 단계: 추출된 레이어를 순서대로 복사합니다.
# 1. 새롭고 깨끗한 이미지에서 다시 시작합니다. (아까 builder의 찌꺼기는 다 사라짐)
FROM eclipse-temurin:21-jre

# 2. 실행할 폴더를 잡습니다.
WORKDIR /application

# [핵심 전략] 무거운 순서대로(잘 안 바뀌는 순서대로) 복사합니다.
# 3. 외부 라이브러리 복사 (수십 MB, 제일 무거움, 하지만 거의 안 바뀜)
COPY --from=builder /builder/dependencies/ ./

# 4. 스프링 부트 구동기 복사 (거의 안 바뀜)
COPY --from=builder /builder/spring-boot-loader/ ./

# 5. 스냅샷 라이브러리 복사 (가끔 바뀜)
COPY --from=builder /builder/snapshot-dependencies/ ./

# 6. 내 소스 코드 복사 (매우 가벼움, 매일 바뀜)
COPY --from=builder /builder/application/ ./

# 7. 실행 명령어 (일반적인 -jar 실행과 다름!)
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]