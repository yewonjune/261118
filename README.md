# 글/댓글 REST API (TDD)

글(Post)과 댓글(PostComment)의 CRUD REST API를 **TDD(테스트 주도 개발)** 로 만든 Spring Boot 프로젝트입니다.
모든 기능을 "실패하는 테스트 먼저(RED) → 통과시키는 구현(GREEN)" 순서로 커밋했고, 각 커밋에 `001` ~ `023` 태그를 달았습니다.

## 기술 스택

- Java 25, Gradle (Kotlin DSL)
- Spring Boot 4.1.2: Web MVC, Data JPA, Validation
- H2 Database (개발: 파일 DB, 테스트: 메모리 DB)
- Lombok
- JUnit 5, MockMvc, AssertJ

## 실행 방법

```bash
./gradlew bootRun
```

- 서버: http://localhost:8080
- H2 콘솔: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:./db_dev;MODE=MySQL`, 사용자 `sa`, 비밀번호 없음)

## 테스트

```bash
./gradlew test
```

테스트는 `test` 프로필(메모리 DB)로 실행되며, 각 테스트가 끝나면 `@Transactional`로 롤백되어 서로 영향을 주지 않습니다.

## 샘플 데이터

앱이나 테스트가 시작될 때 `BaseInitData`가 아래 데이터를 만듭니다. (이미 글이 있으면 만들지 않음)

| 글 | 댓글 (id) |
|---|---|
| 1번 "제목 1" | 1-1 (1), 1-2 (2), 1-3 (3) |
| 2번 "제목 2" | 2-1 (4), 2-2 (5) |
| 3번 "제목 3" | 없음 |

개발 DB의 샘플 데이터를 다시 만들려면 `db_dev.mv.db` 파일을 지우고 실행하세요.

## API

### 글

| 메서드 | URL | 설명 | 성공 | 실패 |
|---|---|---|---|---|
| GET | `/api/v1/posts` | 다건조회 (id 내림차순) | 200 `List<PostDto>` | |
| GET | `/api/v1/posts/{id}` | 단건조회 | 200 `PostDto` | 404 |
| POST | `/api/v1/posts` | 작성 | 201 `RsData<PostDto>` | 400 |
| PUT | `/api/v1/posts/{id}` | 수정 | 200 `RsData<Void>` | 400, 404 |
| DELETE | `/api/v1/posts/{id}` | 삭제 (댓글도 함께 삭제) | 200 `RsData<Void>` | 404 |

요청 본문 (작성/수정)

```json
{ "title": "제목", "content": "내용" }
```

### 댓글

| 메서드 | URL | 설명 | 성공 | 실패 |
|---|---|---|---|---|
| GET | `/api/v1/posts/{postId}/comments` | 다건조회 (id 내림차순) | 200 `List<PostCommentDto>` | 404 |
| GET | `/api/v1/posts/{postId}/comments/{id}` | 단건조회 | 200 `PostCommentDto` | 404 |
| POST | `/api/v1/posts/{postId}/comments` | 작성 | 201 `RsData<PostCommentDto>` | 400, 404 |
| PUT | `/api/v1/posts/{postId}/comments/{id}` | 수정 | 200 `RsData<Void>` | 400, 404 |
| DELETE | `/api/v1/posts/{postId}/comments/{id}` | 삭제 | 200 `RsData<Void>` | 404 |

요청 본문 (작성/수정)

```json
{ "content": "댓글 내용" }
```

댓글은 항상 해당 글 안에서 찾습니다. 다른 글에 달린 댓글 id로 접근하면(예: `/posts/1/comments/4`) 404입니다.

### 응답 형식

조회는 DTO를 그대로, 작성/수정/삭제와 실패는 `RsData`로 감싸서 응답합니다.

```json
{
  "resultCode": "201-1",
  "msg": "4번 글이 작성되었습니다.",
  "data": { "id": 4, "createDate": "...", "modifyDate": "...", "title": "새 제목", "content": "새 내용" }
}
```

| resultCode | 의미 |
|---|---|
| `200-1` | 수정/삭제 성공 |
| `201-1` | 작성 성공 |
| `400-1` | 입력값 검증 실패 (msg: `필드-규칙-메시지`) |
| `400-2` | 요청 본문을 읽을 수 없음 |
| `404-1` | 데이터가 존재하지 않음 |

HTTP 상태코드는 `ResponseAspect`가 `resultCode` 앞자리를 읽어 자동으로 맞춥니다.

## 프로젝트 구조

```
src/main/java/com/back
├── BackApplication.java
├── domain/post
│   ├── post
│   │   ├── controller/ApiV1PostController.java
│   │   ├── dto/PostDto.java
│   │   ├── entity/Post.java
│   │   ├── repository/PostRepository.java
│   │   └── service/PostService.java
│   └── postComment
│       ├── controller/ApiV1PostCommentController.java
│       ├── dto/PostCommentDto.java
│       └── entity/PostComment.java
└── global
    ├── aspect/ResponseAspect.java
    ├── globalExceptionHandler/GlobalExceptionHandler.java
    ├── initData/BaseInitData.java
    ├── jpa/entity/BaseEntity.java
    └── rsData/RsData.java
```

댓글은 글(`Post`)을 통해서만 생성, 조회, 삭제합니다(애그리거트 루트). 그래서 댓글 전용 리포지토리는 없습니다.

## 커밋 따라가기

각 단계는 태그로 확인할 수 있습니다. 커밋 메시지에 요구사항, 변경 내용, 설계 이유를 자세히 적어 두었습니다.

```bash
git checkout 007        # 7단계 시점의 코드 보기
git diff 004 005        # RED → GREEN 사이에 바뀐 코드 보기
git checkout main       # 최신 코드로 돌아오기
```

| 태그 | 단계 | 내용 |
|---|---|---|
| 001 | | 프로젝트 생성 (Spring Boot, JPA, H2 설정) |
| 002 | | 엔티티, 리포지토리, 서비스 뼈대 |
| 003 | | 샘플 데이터 (글 3개, 댓글 5개) |
| 004 | RED | 글 다건조회 테스트 |
| 005 | GREEN | 글 다건조회 구현 (`PostDto`) |
| 006 | RED | 글 단건조회 테스트 |
| 007 | GREEN | 글 단건조회 구현 (`RsData`, 전역 예외 처리) |
| 008 | RED | 글 작성 테스트 |
| 009 | GREEN | 글 작성 구현 (입력값 검증, `ResponseAspect`) |
| 010 | RED | 글 수정 테스트 |
| 011 | GREEN | 글 수정 구현 (변경 감지) |
| 012 | RED | 글 삭제 테스트 |
| 013 | GREEN | 글 삭제 구현 |
| 014 | RED | 댓글 다건조회 테스트 |
| 015 | GREEN | 댓글 다건조회 구현 (`PostCommentDto`) |
| 016 | RED | 댓글 단건조회 테스트 |
| 017 | GREEN | 댓글 단건조회 구현 (`findCommentById`) |
| 018 | RED | 댓글 작성 테스트 |
| 019 | GREEN | 댓글 작성 구현 (`flush`로 id 확보) |
| 020 | RED | 댓글 수정 테스트 |
| 021 | GREEN | 댓글 수정 구현 |
| 022 | RED | 댓글 삭제 테스트 |
| 023 | GREEN | 댓글 삭제 구현 (`orphanRemoval`) |
