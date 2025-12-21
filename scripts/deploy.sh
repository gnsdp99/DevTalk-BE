#!/bin/bash

# 1. 현재 실행 중인 컨테이너 확인 (Blue가 켜져 있는지 확인)
IS_BLUE=$(docker ps | grep app-blue)

if [ -z "$IS_BLUE" ]; then
  echo "### BLUE => GREEN 배포 시작 ###"
  TARGET_CONTAINER="app-blue"
  TARGET_PORT=8081
  STOP_CONTAINER="app-green"
else
  echo "### GREEN => BLUE 배포 시작 ###"
  TARGET_CONTAINER="app-green"
  TARGET_PORT=8082
  STOP_CONTAINER="app-blue"
fi

# 2. 새 버전 이미지 빌드 (캐시 활용을 위해 build 명령 사용)
echo "1. 이미지 빌드 중..."
docker-compose build $TARGET_CONTAINER

# 3. 새 컨테이너 실행
echo "2. $TARGET_CONTAINER 컨테이너 실행..."
docker-compose up -d $TARGET_CONTAINER

# 4. Health Check (스프링 부트가 뜰 때까지 대기)
echo "3. Health Check 시작 (http://localhost:$TARGET_PORT/actuator/health)..."
for i in {1..10}; do
  # /actuator/health 혹은 메인 페이지(/) 호출. 응답코드 200 확인
  RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$TARGET_PORT/actuator/health)

  if [ "$RESPONSE" -eq 200 ]; then
    echo ">> Health Check 성공!"
    break
  else
    echo ">> 응답 없음... 대기 중 ($i/10)"
    sleep 10
  fi

  if [ $i -eq 10 ]; then
    echo ">> Health Check 실패. 배포를 중단합니다."
    exit 1
  fi
done

# 5. Nginx 방향 전환 (service-url.inc 파일 덮어쓰기)
echo "4. Nginx 트래픽 전환 ($TARGET_CONTAINER)"
# Docker 내부 네트워크 주소(app-blue or app-green)를 사용해야 함
docker exec nginx sh -c \
"echo 'set \$service_url http://$TARGET_CONTAINER:8080;' > /etc/nginx/conf.d/service-url.inc"

# 6. Nginx 설정 Reload (무중단)
echo "5. Nginx Reload"
docker exec nginx nginx -s reload

# 7. 이전 컨테이너 종료
echo "6. 이전 컨테이너($STOP_CONTAINER) 종료"
docker-compose stop $STOP_CONTAINER

echo "### 배포 완료! ###"