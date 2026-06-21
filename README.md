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
  FileReader (CSV)
    └── Chunk Step (Reader → Processor → Writer)
          ├── SalesRawDataValidator (Processor)
          └── SalesRawDataRepository (Writer)
                └── MySQL (sales_raw_data)

[Job 2] salesAggregationJob
  Tasklet / Chunk Step
    ├── DailySalesAggregator
    ├── MonthlySalesAggregator
    └── SalesSummaryRepository (Writer)
          └── MySQL (sales_summary_daily / sales_summary_monthly)

  └── JobExecutionListener
        ├── EmailReportSender
        └── DashboardSummaryWriter
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

- Java 17+
- MySQL 8.0+
- SMTP 계정 (이메일 발송용)

### 설정

```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sales_batch_db
    username: your_username
    password: your_password
  batch:
    jdbc:
      initialize-schema: always   # Batch 메타테이블 자동 생성
  mail:
    host: smtp.gmail.com
    port: 587
    username: your_email@gmail.com
    password: your_app_password

report:
  recipient: manager@example.com

scheduler:
  csv-import-cron: "0 0 1 * * *"        # 매일 새벽 1시 CSV 적재
  aggregation-cron: "0 0 2 * * *"       # 매일 새벽 2시 집계 실행
```

### 실행

```bash
git clone https://github.com/david-lab/sales-batch-processor.git
cd sales-batch-processor
./gradlew bootRun
```

### CSV 직접 실행 (테스트용)

```bash
java -jar build/libs/sales-batch-processor.jar \
  --job.name=csvImportJob \
  --filePath=/path/to/sales_data.csv
```

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
