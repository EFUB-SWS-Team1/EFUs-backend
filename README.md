<img width="1920" height="1080" alt="19" src="https://github.com/user-attachments/assets/79c16a34-731c-4ac3-a22d-df5394a8798a" />



```
단체의 회비와 행사 예산을 더 쉽고 투명하게 관리하는 서비스, EFUs 💸
EFUB SWS 1팀 EFUs 프로젝트입니다.
```

</br>

# 🧑🏻‍💻 Backend Developer

| 이현정 | 진웨이얀 | 이현경 |
|:---:|:---:|:---:|
| [@KKANGCHONG](https://github.com/KKANGCHONG) | [@ZinYan](https://github.com/ZinYan) | [@eluda315](https://github.com/eluda315) |
| <img src="https://github.com/user-attachments/assets/848187dd-36d5-4673-8cdb-421f78bcf544" width="200"> | <img src="https://github.com/user-attachments/assets/95bd1a76-ca43-4240-a679-8d5f358362b9" width="200"> | <img src="https://github.com/user-attachments/assets/fed1eca5-4540-4705-beb7-6280c43b4f93" width="200"> |
| `Member`</br>`Invitation`</br>`Charge` |  `AWS S3`</br>`Transaction`</br>`Receipt` | `kakao login`</br>`Organization`</br>`Term` |

</br>

# 🗺️ ERD

<img width="2030" height="1252" alt="EFUs project ERD" src="https://github.com/user-attachments/assets/b49f7737-494b-4dd3-b381-b5e951bafb31" />


</br>

# 💸 Main Feature

| 단체 · 기수 관리 | 구성원 · 초대 관리 | 회비 관리 |
|:---:|:---:|:---:|
| 단체 생성 및 첫 기수 등록</br>기수 생성 · 수정 · 종료</br>기수별 대시보드 | 기수별 구성원 조회</br>STAFF / MEMBER 권한 관리</br>역할별 초대 코드 발급 | 회비 청구 및 N빵 계산</br>개별 · 일괄 납부 처리</br>미납 현황 관리 |

</br>

| 행사 · 예산 관리 | 가계부 관리 | 영수증 · 이력 관리 |
|:---:|:---:|:---:|
| 행사 등록 · 수정</br>행사별 예산 및 사용률 조회 | 수입 · 지출 등록</br>통합 가계부 조회</br>거래 소프트 삭제 | AWS S3 영수증 관리</br>Presigned URL 조회</br>거래 · 회비 변경 이력 기록 |

</br>

# 🏗 Design System

<img width="1440" alt="EFUs Design System" src="https://github.com/user-attachments/assets/89527c7d-f950-4a6d-9c30-53208f87e067" />

</br>

### Core Domain

```text
User
 └── Organization
      └── OrganizationTerm
           ├── TermMember
           │    ├── Invitation
           │    └── ChargeMember
           │
           ├── Funding
           │    ├── Transaction
           │    └── Charge
           │
           ├── Transaction
           │    ├── Receipt
           │    └── TransactionHistory
           │
           └── Charge
                ├── ChargeMember
                └── ChargeHistory
```

</br>

# ⚙️ Tech Stack

| Category | Technology | Description |
|:---:|:---:|:---|
| **Language** | Java 17 | 백엔드 서버 개발 |
| **Framework** | Spring Boot | REST API 서버 구현 |
| **ORM** | Spring Data JPA | 객체 기반 데이터베이스 접근 |
| **Database** | MySQL | 서비스 데이터 저장 |
| **Authentication** | Spring Security · JWT | Access / Refresh Token 기반 인증 |
| **Social Login** | Kakao Login | 카카오 OAuth 기반 로그인 |
| **Storage** | AWS S3 | 영수증 이미지 비공개 저장 |
| **Build** | Gradle | 프로젝트 빌드 및 의존성 관리 |
| **Test DB** | H2 | 테스트 환경 데이터베이스 |
| **Deploy** | AWS EC2 · Docker | 백엔드 애플리케이션 컨테이너 배포 |
| **Web Server** | Nginx | Reverse Proxy 및 HTTPS 연결 |
| **CI/CD** | GitHub Actions | 빌드 · 테스트 · 자동 배포 |
| **Container Registry** | Docker Hub | Docker Image 저장 및 배포 |

</br>

# 📂 Foldering

```text
📁 src
└── 📁 main
    ├── 📁 java
    │   └── 📁 com.efus.backend
    │       │
    │       ├── 📁 domain
    │       │   ├── 📁 auth
    │       │   │   ├── controller
    │       │   │   ├── dto
    │       │   │   ├── entity
    │       │   │   ├── repository
    │       │   │   └── service
    │       │   │
    │       │   ├── 📁 user
    │       │   ├── 📁 organization
    │       │   ├── 📁 term
    │       │   ├── 📁 member
    │       │   ├── 📁 invitation
    │       │   ├── 📁 funding
    │       │   ├── 📁 transaction
    │       │   ├── 📁 receipt
    │       │   ├── 📁 charge
    │       │   └── 📁 history
    │       │
    │       └── 📁 global
    │           ├── 📁 config
    │           ├── 📁 domain
    │           │   └── BaseEntity
    │           ├── 📁 exception
    │           │   ├── CustomException
    │           │   ├── ErrorCode
    │           │   └── GlobalExceptionHandler
    │           ├── 📁 response
    │           │   └── ApiResponse
    │           └── 📁 security
    │               └── jwt
    │
    └── 📁 resources
        └── application.yml
```

</br>

# 📚 API

EFUs의 전체 API 명세는 아래 문서에서 확인할 수 있습니다.

[EFUs API 명세서 보러가기 ✔️](https://efub.notion.site/API-31fe1ad1c5f080b7bdf2ed9ddc19220c?source=copy_link)

</br>

# 💥 Trouble Shooting

EFUs 백엔드 개발 과정에서 발생한 문제와 해결 과정을 정리했습니다.

[EFUs 백엔드 트러블 슈팅 보러가기 ✔️](https://efub.notion.site/3aae1ad1c5f080458de7e0bde1c96dff?source=copy_link)

</br></br>
