# spring-security-jwt

Spring Boot + React + MySQL 기반으로, Spring Security와 JWT, OAuth2 소셜 로그인을 학습하기 위해 진행한 실습 프로젝트입니다. 폼 로그인과 JWT 인증, 소셜 로그인(OAuth2)을 하나의 인증 흐름으로 통합했으며, DB와 Redis는 로컬 환경에서 Docker로 구동합니다.

Access Token / Refresh Token 기반의 인증 구조를 직접 구현하는 데 초점을 두었습니다.

- **Access Token**: 클라이언트 상태(메모리)로 관리 — XSS로 인한 탈취 위험이 있어 별도의 보완 설정이 필요
- **Refresh Token**: HttpOnly 쿠키로 관리 — XSS로부터 비교적 안전하며, Access Token 재발급 용도로만 사용되어 CSRF에 상대적으로 덜 민감

## 목차

- [기술 스택](#기술-스택)
  - [백엔드](#백엔드)
  - [프론트엔드](#프론트엔드)
- [백엔드](#백엔드-1)
  - [Spring Security 필터 구조](#spring-security-필터-구조)
  - [JWT](#jwt)
  - [OAuth2 소셜 로그인](#oauth2-소셜-로그인)
  - [로그인 (폼 로그인)](#로그인-폼-로그인)
  - [회원가입](#회원가입)
  - [JWT 생성 · 저장 및 재발급](#jwt-생성--저장-및-재발급)
  - [로그아웃](#로그아웃)
  - [회원 탈퇴](#회원-탈퇴)
- [프론트엔드](#프론트엔드-1)
  - [회원가입](#회원가입-1)
  - [로그인](#로그인-1)
  - [로그아웃](#로그아웃-1)
  - [회원 탈퇴](#회원-탈퇴-1)
  - [인증 상태 공유](#인증-상태-공유)

## 기술 스택

### 백엔드

![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

### 프론트엔드

![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![React Router](https://img.shields.io/badge/React%20Router-CA4245?style=for-the-badge&logo=reactrouter&logoColor=white)

## 백엔드

### Spring Security 필터 구조

세션 대신 JWT를 사용하므로 `SessionCreationPolicy.STATELESS`로 설정하고, 매 요청마다 토큰을 검증하는 커스텀 필터(`JwtFilter`)를 `LogoutFilter` 앞에 등록하는 구조입니다.

- 요청 헤더의 `Authorization: Bearer {accessToken}`을 파싱
- 토큰이 유효하면 `SecurityContextHolder`에 인증 정보(Authentication)를 저장
- 토큰이 없거나 유효하지 않으면 다음 필터로 넘기고, 이후 인증이 필요한 요청에서 401/403으로 걸러짐
- 커스텀 로그인 필터(`LoginFilter`), JWT 검증 필터가 하나의 `SecurityFilterChain`에 함께 구성됨

### JWT

- **Access Token**: 짧은 만료 시간(예: 5분 내외)을 가지며, 인가에 필요한 유저 식별 정보(예: userId, username, role)를 payload에 포함
- **Refresh Token**: 상대적으로 긴 만료 시간(예: 1~7일)을 가지며, Access Token 재발급 용도로만 사용
- HS256 등의 대칭키 서명 방식으로 토큰의 위변조 여부를 검증
- 토큰 파싱/검증 로직은 JwtUtil, JwtFilter 클래스에서 담당

### OAuth2 소셜 로그인

- Google, Naver를 OAuth2 Provider로 등록하여 소셜 로그인 지원
- `DefaultOAuth2UserService`를 상속한 `UserService`에서 Provider별로 다른 유저 정보 응답 포맷을 `OAuth2Response`를 통해 공통 형태로 변환
- 로그인 성공 시 `OAuth2SuccessHandler`에서 JWT를 발급하고 프론트엔드로 리다이렉트
- 로그인 실패(사용자 동의 취소, 인증 코드 오류 등) 시 `OAuth2FailureHandler`에서 에러 정보를 쿼리 파라미터에 담아 프론트엔드로 리다이렉트
- 소셜 로그인은 JS가 직접 제어하지 못하는 브라우저 리다이렉트이기 때문에 바디 전달이 불가하여 쿠키로 Refresh Token만 전달
- 이후 프론트에서 AuthProvider를 통해 바로 Access Token 재발급 API 호출

### 로그인 (폼 로그인)

- Username/Password 기반 로그인 API 제공
- `AuthenticationManager`를 통해 자격 증명을 검증
- 로그인 성공 시 Access Token은 응답 바디로, Refresh Token은 HttpOnly 쿠키로 내려줌

### 회원가입

- Username 중복 여부 검증
- 비밀번호는 `BCryptPasswordEncoder`로 암호화하여 저장
- 가입 완료 후 별도 로그인 절차를 거치도록 구성

### JWT 생성 · 저장 및 재발급

- 로그인 성공 시 Access Token과 Refresh Token을 발급
- Refresh Token은 서버 측(Redis)에도 저장하여, 클라이언트가 가진 쿠키 값과 대조 검증 가능
- Access Token 만료 시 `/auth/reissue` API로 Refresh Token(쿠키)을 검증한 뒤 새 Access Token을 재발급
- Refresh Token까지 만료/무효화된 경우 재로그인을 유도

### 로그아웃

- 클라이언트의 Access Token 상태를 초기화
- 서버에 저장된 Refresh Token(Redis)을 제거하고, 쿠키도 만료 처리하여 재사용 불가하도록 처리

### 회원 탈퇴

- 탈퇴 요청 시 사용자 인증 상태(Access Token) 확인 후 처리
- 탈퇴 완료 시 해당 유저의 Refresh Token(Redis)도 함께 제거
- 탈퇴 후 클라이언트의 인증 상태를 초기화하고 홈 화면으로 이동

## 프론트엔드

### 회원가입

- Username/Password 등 회원 정보를 입력받는 회원가입 페이지
- 입력값 검증 후 백엔드 회원가입 API 호출
- 가입 성공 시 홈으로 이동

### 로그인

- Username/Password 폼 로그인과 Google/Naver 소셜 로그인 버튼을 함께 제공
- 폼 로그인 성공 시 응답으로 받은 Access Token을 Context 상태에 저장
- 소셜 로그인은 백엔드의 `/oauth2/authorization/{provider}` 경로로 이동시켜 진입

### 로그아웃

- 로그아웃 API 호출 후 Access Token 상태를 초기화
- 이후 인증이 필요한 페이지 접근 시 홈으로 리다이렉트

### 회원 탈퇴

- 탈퇴 API 호출 성공 시 Access Token 상태를 초기화
- 탈퇴 완료 후 홈 화면으로 이동

### 인증 상태 공유

- `AuthContext` + `AuthProvider`로 Access Token, 로딩 상태를 앱 전역에서 공유
- 앱 최초 로드 시 Refresh Token(쿠키)으로 Access Token 재발급을 시도해 로그인 상태를 복원
- 복원이 끝나기 전(`loading === true`)에는 하위 페이지를 렌더링하지 않아, 인증 상태가 확정된 이후에만 각 페이지가 마운트되도록 구성
- 만료된 Access Token으로 API 요청 시, 공통 fetch 함수(`useAuthFetch`)에서 자동으로 재발급을 시도한 뒤 원래 요청을 재시도
