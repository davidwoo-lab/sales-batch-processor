# 📦 Sales Batch Processor

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot)
![Spring Batch](https://img.shields.io/badge/Spring_Batch-5.x-6DB33F?style=for-the-badge&logo=spring)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql)

CSV로 업로드된 원본 매출 데이터를 DB에 적재하고, 이를 기반으로 일/월 매출을 집계하여 결과를 이메일로 발송하고 대시보드용 테이블에 저장하는 Spring Batch 기반 배치 처리 샘플 프로젝트입니다.

---

## 📌 주요 기능

| 기능 | 설명 |
|---|---|
| CSV 적재 배치 | 대용량 CSV 파일을 청크 단위로 읽어 DB에 저장 |
| 매출 집계 배치 | 적재된 원본 데이터를 기준으로 일별 / 월별 매출 집계 |
| 결과 통보 | 집계 완료 후 담당자에게 요약 리포트 이메일 발송 |
| 대시보드 연동 | 집계 결과를 별도 테이블에 저장해 조회·시각화에 활용 |
| 배치 이력 관리 | Spring Batch 메타테이블로 실행 이력·재시작 관리 |
| 실패 복구 | 청크 단위 실패 시 해당 지점부터 재시작 가능 |

---

## 🏗️ 아키텍처

```
[Job 1] csvImportJob
  ① purgeRawDataStep (Tasklet)        # targetDate 기존 데이터 삭제 → 재적재 멱등성 보장
  ② csvImportStep (Chunk: Reader → Processor → Writer)
        ├── SalesDataValidator (Processor, 검증 실패 시 skip)
        └── SalesRawDataItemWriter (JpaItemWriter)
              └── MySQL (sales_raw_data)

[Job 2] salesAggregationJob
  ① dailyAggregationStep (Tasklet)    # GROUP BY 집계 → sales_summary_daily upsert
  ② monthlyAggregationStep (Tasklet)  # 월 범위 집계 → sales_summary_monthly upsert

  └── AggregationJobListener (afterJob)
        ├── 성공 시: DashboardSummaryService(스냅샷 저장) + EmailReportSender(요약 리포트)
        └── 실패 시: EmailReportSender(실패 알림)
```

### 패키지 구조

```
src/main/java/com/davidlab/salesbatch/
├── common/
│   ├── config/           # Batch, JPA, Mail 설정
│   └── exception/        # 전역 예외 처리
├── batch/
│   ├── csvimport/
│   │   ├── CsvImportJobConfig.java
│   │   ├── SalesCsvItemReader.java
│   │   ├── SalesDataValidator.java   (Processor)
│   │   └── SalesRawDataItemWriter.java
│   ├── aggregation/
│   │   ├── SalesAggregationJobConfig.java
│   │   ├── DailySalesAggregationTasklet.java
│   │   ├── MonthlySalesAggregationTasklet.java
│   │   └── listener/
│   │       └── AggregationJobListener.java  # 이메일 + 대시보드 트리거
│   └── trigger/
│       └── BatchScheduler.java        # @Scheduled로 Job 실행
├── sales/
│   ├── domain/            # SalesRawData, SalesSummaryDaily, SalesSummaryMonthly
│   └── repository/
├── notification/
│   └── EmailReportSender.java
└── dashboard/
    └── DashboardSummaryService.java
```

---

## 🛠️ 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| 배치 프레임워크 | Spring Batch 5.x |
| ORM | Spring Data JPA (Hibernate) |
| DB | MySQL 8.0 |
| 메일 발송 | Spring Mail (JavaMailSender) |
| 스케줄러 | Spring Scheduler (@Scheduled, 배치 트리거용) |
| 빌드 | Gradle |

---

## 🚀 실행 방법

### 사전 요구사항

- Docker / Docker Compose (권장 — 가장 빠른 실행 경로)
- 또는 직접 실행 시: Java 17+, MySQL 8.0+
- SMTP 계정 (이메일 발송용, 미설정 시 발송만 생략되고 배치는 정상 동작)

### 🐳 빠른 실행 (Docker Compose)

별도 설치 없이 MySQL과 애플리케이션을 한 번에 기동합니다.

```bash
git clone https://github.com/davidwoo-lab/sales-batch-processor.git
cd sales-batch-processor
docker compose up --build
```

기동 후:

- 애플리케이션: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- MySQL: `localhost:3306` (db: `sales_batch_db` / user: `sales` / pw: `sales1234`)
- `./sample-data` 가 컨테이너의 `/data/sales/import` 로 마운트되어 바로 적재 테스트 가능

### 환경변수 설정

민감 정보는 코드에 두지 않고 환경변수로 주입합니다. (`.env.example` 참고)

| 환경변수 | 설명 |
|---|---|
| `DB_USERNAME` / `DB_PASSWORD` | DB 접속 정보 |
| `MAIL_USERNAME` / `MAIL_APP_PASSWORD` | SMTP 계정 (Gmail은 앱 비밀번호) |
| `REPORT_RECIPIENT` | 결과 리포트 수신자 |
| `CSV_IMPORT_PATH` | 스케줄러가 읽을 CSV 디렉토리 (`{경로}/{yyyy-MM-dd}.csv`) |

### 직접 실행 (로컬 MySQL)

```bash
./gradlew bootRun
```

### 수동 실행 & 조회 (REST API)

```bash
# 1) CSV 적재 (targetDate 지정 시 해당 일자 재적재 멱등성 보장)
curl -X POST "http://localhost:8080/api/batch/csv-import?filePath=/data/sales/import/sales_data_sample.csv&targetDate=2026-06-01"

# 2) 매출 집계 (일/월 집계 + 이메일 + 대시보드)
curl -X POST "http://localhost:8080/api/batch/aggregation?targetDate=2026-06-01"

# 3) 결과 조회
curl "http://localhost:8080/api/sales/daily?date=2026-06-01"     # 일별 집계
curl "http://localhost:8080/api/sales/monthly?month=2026-06"     # 월별 집계
curl "http://localhost:8080/api/dashboard"                       # 대시보드 스냅샷(최신순)
```

> `적재 → 집계 → 조회` 전 과정을 Swagger UI(http://localhost:8080/swagger-ui.html)에서 클릭만으로 확인할 수 있습니다.

---

## 📄 CSV 입력 포맷 예시

```csv
order_date,store_id,product_name,quantity,unit_price,total_amount
2026-06-01,STORE001,아메리카노,3,4500,13500
2026-06-01,STORE001,카페라떼,2,5000,10000
2026-06-01,STORE002,아메리카노,5,4500,22500
```

---

## 📊 집계 결과 테이블 예시

### sales_summary_daily

| sales_date | store_id | total_quantity | total_amount |
|---|---|---|---|
| 2026-06-01 | STORE001 | 5 | 23,500 |
| 2026-06-01 | STORE002 | 5 | 22,500 |

### sales_summary_monthly

| sales_month | store_id | total_quantity | total_amount |
|---|---|---|---|
| 2026-06 | STORE001 | 152 | 689,000 |

---

## 📧 결과 이메일 예시

```
제목: [매출 배치] 2026-06-01 집계 완료

안녕하세요, 2026-06-01 매출 집계 결과를 안내드립니다.

- 처리 건수: 1,204건
- 총 매출: 32,560,000원
- 처리 매장 수: 18개
- 실패 건수: 0건

상세 내역은 대시보드에서 확인 가능합니다.
```

---

## 📄 License

MIT
