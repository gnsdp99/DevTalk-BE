# DevTalk - 개발자 지식 공유 커뮤니티 서비스 (BE)

DevTalk은 개발자들이 편하게 질문하고 답하며 지식을 공유할 수 있는 커뮤니티 서비스입니다

## 프로젝트 개요

- 개발 기간: 2025.09.22 ~ 2025.12.07
- 개발 인원: 1명 (개인 프로젝트)
- [FE Repo](https://github.com/100-hours-a-week/3-ian-kim-community-fe)

## 주요 기능

- 회원가입/로그인
- 게시글 CRUD
- 게시글 목록 조회
- 게시글 좋아요
- 댓글 CRUD
- 댓글 목록 조회

## 기술 스택

<table >
    <tr>
        <td>Language</td>
        <td><code>Java 21</code></td>
    </tr>
    <tr>
        <td>Framework</td>
        <td><code>Spring Boot 3.5.6</code>, <code>Spring Data JPA 3.5.4</code>, <code>Spring Security 6.5.5</code></td>
    </tr>
    <tr>
        <td>Rate Limiting</td>
        <td><code>Bucket4j 8.15.0</code></td>
    </tr>
    <tr>
        <td>Caching</td>
        <td><code>Caffeine 3.2.3</code></td>
    </tr>
    <tr>
        <td>Testing</td>
        <td><code>Junit5</code>, <code>Mockito</code>, <code>Jacoco</code></td>
    </tr>
    <tr>
        <td>API Documentation</td>
        <td><code>Springdoc Openapi</code></td>
    </tr>
    <tr>
        <td>Database</td>
        <td><code>MySQL 9.4.0</code>, <code>H2 DB 2.3.232</code></td>
    </tr>
</table>

## ERD

<img width="1663" height="660" alt="ERD" src="https://github.com/user-attachments/assets/acc9b63b-4076-4ca5-bbaf-d888305ed396" />

## 주요 개발 내용

### 벌크 업데이트로 회원탈퇴 및 게시글 삭제 성능 개선

#### 상황

- 회원 탈퇴 시 회원이 작성한 게시글과 댓글을 삭제하지 않고 익명화하는 요구사항 존재
- 게시글 삭제 시 게시글에 작성된 댓글을 soft delete 처리하는 요구사항 존재

#### 문제점

- JPA Dirty Checking을 통한 업데이트는 단건으로 처리되어, 회원이 작성한 게시글 및 댓글 수에 비례해 UPDATE 쿼리가 발생
- 결국 데이터 수에 비례해 DB I/O 및 변경 감지 비용 발생

#### 해결 방법

- `@Query`로 DB에 직접 벌크 업데이트하여, 단일 UPDATE 쿼리로 일괄 변경
- DB와 영속성 컨텍스트의 데이터 불일치 문제 없도록 구현

#### 결과

- 회원 탈퇴 시 익명화 응답 속도 1m 52s → 3s로 개선 (게시글 100,000건, 댓글 100,000건 기준)
- 게시글 삭제 시 댓글 삭제 속도 20s → 600ms로 개선 (댓글 100,000건 기준)

---

### 인덱스로 게시글 목록 조회 성능 개선

#### 상황

- 게시글 목록 조회 시 최근 생성일 기준으로 정렬된 상위 N건을 조회하는 요구사항 존재
- 삭제되지 않은 게시글만 조회

#### 문제점

- 적절한 인덱스가 없어 게시글 테이블 전체 스캔 및 정렬 연산 발생

#### 해결 방법

- (`is_deleted`, `created_at`) 복합 인덱스 생성 (DESC는 영향 없음)
- 게시글은 읽기 빈도가 쓰기 빈도보다 훨씬 크므로 쓰기 성능의 트레이드오프 감수

#### 결과

- 게시글 목록 조회 응답 속도 500ms → 44ms로 개선 (100개 기준)
- 실행 계획 Table scan + Filter + Sort → Index lookup

```text
// before
-> Limit: 100 row(s)  (cost=32505 rows=100)
    -> Nested loop left join  (cost=32505 rows=99498)
        -> Sort row IDs: p1_0.created_at DESC, limit input to 100 row(s) per chunk  (cost=10118 rows=99498)
            -> Filter: (p1_0.is_deleted = 0)  (cost=10118 rows=99498)
                -> Table scan on p1_0  (cost=10118 rows=99498)
        -> Single-row index lookup on u1_0 using PRIMARY (user_id = p1_0.user_id)  (cost=0.25 rows=1)
        
// after
-> Limit: 100 row(s)  (cost=22892 rows=100)
    -> Nested loop left join  (cost=22892 rows=49749)
        -> Index lookup on p1_0 using idx_post_deleted_created_at (is_deleted = 0) (reverse)  (cost=5480 rows=49749)
        -> Single-row index lookup on u1_0 using PRIMARY (user_id = p1_0.user_id)  (cost=0.25 rows=1)
```

---

### 락 범위 최소화로 동시성 처리 성능 개선

#### 상황

- 게시글 조회 시 조회수 증가시키는 로직에 동시성 문제 발생

#### 문제점

- 비관적 락을 적용하면 행 단위로 쓰기 락이 걸려, 다른 트랜잭션에서 동일 게시글 조회 시 조회수를 증가시키지 못하고 대기하는 문제 발생
- 이로 인해 트래픽이 증가할수록 대기 시간이 누적되어 지연 시간이 증가

#### 해결 방법

- DB에 직접 UPDATE 쿼리를 실행해 조회수를 원자적으로 증가
- 락의 범위를 단일 UPDATE 쿼리 실행 시간으로 최소화하여, 연산 종료 즉시 다른 트랜잭션에서도 쓰기 가능하도록 개선 

#### 결과

- 게시글 조회 응답 속도 3s → 2s로 개선 (1000명 동시 요청 기준)

---

### 서버의 안정성을 높이기 위한 Rate Limiting

- 서비스 운영 시에는 무분별한 요청으로부터 서버를 보호할 필요가 있습니다. 요청을 제한하지 않으면 악의적 요청으로 인해 서버의 자원이 고갈되고, 이는 비용 증가 및 서비스 품질 저하로 이어질 수 있습니다. 따라서 API 서버 차원에서 요청을 제어함으로써 서버의 안정성을 확보하고자 했습니다 


- Rate Limiting 기반 알고리즘으로는 사용자 경험을 해치지 않으면서 평균 요청률을 제한해 서버의 안정성을 높일 수 있는 Token Bucket 알고리즘을 선택했습니다.

#### Rate Limiting 정책

- 로그인 요청은 무차별 대입 공격으로부터 계정 탈취를 보호해야 하기 때문에 다른 요청보다 엄격한 제한 정책을 적용했습니다. 이를 위해 로그인 요청에 대한 식별 키로는 IP 주소와 이메일 주소 두 가지를 사용했습니다.


- 이를 통해 공격자가 서로 다른 IP를 이용해 동일 이메일을 공격할 수 없고, 동일 IP에서 여러 이메일을 동시에 공격할 수 없도록 제한했습니다.

#### 캐시로 효율적인 메모리 관리

- 모든 요청마다 Rate Limiting 상태를 관리해야 하므로 요청 수가 늘어날수록 메모리 사용량이 증가하게 됩니다. 따라서 메모리를 효율적으로 관리하기 위해서는 더 이상 관리할 필요가 없는 상태를 제거해주어야 합니다.


- Token Bucket 알고리즘에서는 일정 시간 동안 요청이 없으면 자연스럽게 버킷에 토큰이 가득 차, 제한 초기 상태가 됩니다. 이 상태에서는 더 이상 요청이 없으면 상태를 관리할 필요가 없다고 판단해 제거 대상으로 만들었습니다.


- 이를 위해 로컬 캐시인 Caffeine의 `expireAfterAccess` 정책을 이용해, 토큰이 가득 찰 때까지 요청이 발생하지 않은 상태를 자동으로 정리하도록 구현했습니다. 

---

### 테스트 전략

- 테스트 코드는 리팩토링 내성이 중요하다고 생각해 구현이 아닌 설계를 테스트하는 데 집중했습니다. 특히 과도한 모킹은 테스트와 구현을 강하게 결합하게 만드므로 사용을 최대한 지양했습니다.

#### Service 계층 

- Service 계층은 로직이 대부분 DB와의 상호작용으로 이루어지는데, 단위 테스트를 작성하기 위해 의존성을 과도하게 모킹하기 보다는 `@SpringBootTest` 기반 통합 테스트를 작성했습니다.

#### Repository 계층

- Repository 계층은 커스텀 쿼리 메서드의 동작을 검증하기 위해 `@DataJpaTest` 기반 슬라이스 테스트를 작성했습니다.

#### Controller 계층

- Controller 계층은 HTTP 요청, 응답 데이터를 검증하기 위해 `@WebMvcTest` 기반 슬라이스 테스트를 작성했습니다.

---

### 테스트 커버리지

- 테스트 커버리지는 '테스트가 안된 코드를 인지', '테스트 케이스가 의도대로 동작하는 검증'하는 용도로 사용했습니다.


- 테스트 커버리지가 테스트 코드의 품질을 보장하지는 않지만, 테스트된 코드가 그렇지 않은 코드보단 많아야 한다고 생각합니다. 따라서 테스트 커버러지를 최소 80% 이상 달성할 때만 빌드에 성공하도록 설정했습니다.

#### [테스트 커버리지 리포트를 통해 불필요한 운영 코드 발견](https://github.com/100-hours-a-week/3-ian-kim-community-be/blob/main/docs/%ED%85%8C%EC%8A%A4%ED%8A%B8%20%EC%BB%A4%EB%B2%84%EB%A6%AC%EC%A7%80%20%EB%A6%AC%ED%8F%AC%ED%8A%B8%EB%A5%BC%20%ED%86%B5%ED%95%B4%20%EB%B6%88%ED%95%84%EC%9A%94%ED%95%9C%20%EC%9A%B4%EC%98%81%20%EC%BD%94%EB%93%9C%20%EB%B0%9C%EA%B2%AC.md)

<img width="700" height="700" alt="Image" src="https://github.com/user-attachments/assets/00c9b244-79b3-4583-aaa6-1d251e52cc07" />