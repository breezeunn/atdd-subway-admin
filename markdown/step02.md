# 2단계 - 인수 테스트 리팩터링

## 1. 요구사항 정의

### 1.1. 명시된 요구사항

#### 1.1.1. 요구사항

##### 1.1.1.0. API 변경 대응하기
- 노선 생성 시 종점역(상행, 하행) 정보를 요청 파라미터에 함께 추가하기
    - 두 종점역은 구간의 형태로 관리되어야 함
- 노선 조회 시 응답 결과에 역 목록 추가하기
    - 상행역 부터 하행역 순으로 정렬되어야 함

##### 1.1.1.1. 변경사항

- 생성
    - 변경된 노선 API - 노선 생성 Request
        ```json
        POST /lines HTTP/1.1
        accept: */*
        content-type: application/json; charset=UTF-8
        
        {
            "color": "bg-red-600",
            "name": "신분당선",
            "upStationId": "1",
            "downStationId": "2",
            "distance": "10"
        }
        ```
- 목록 조회
    - 변경된 노선 API - 지하철 노선 목록 조회 response
        ```json
        HTTP/1.1 200
        Content-Type: application/json
        Date: Fri, 13 Nov 2020 00:11:51 GMT
        
        [
            {
                "id": 1,
                "name": "신분당선",
                "color": "bg-red-600",
                "stations": [
                    {
                        "id": 1,
                        "name": "강남역",
                        "createdDate": "2020-11-13T12:17:03.075",
                        "modifiedDate": "2020-11-13T12:17:03.075"
                    },
                    {
                        "id": 2,
                        "name": "역삼역",
                        "createdDate": "2020-11-13T12:17:03.092",
                        "modifiedDate": "2020-11-13T12:17:03.092"
                    }
                ],
                "createdDate": "2020-11-13T09:11:51.997",
                "modifiedDate": "2020-11-13T09:11:51.997"
            }
        ]
        ```
- 조회
    - 변경된 노선 API - 노선 조회 Response
        ```json
        HTTP/1.1 200 
        Content-Type: application/json
        Date: Fri, 13 Nov 2020 00:11:51 GMT
        
        {
            "id": 1,
            "name": "신분당선",
            "color": "bg-red-600",
            "stations": [
                {
                    "id": 1,
                    "name": "강남역",
                    "createdDate": "2020-11-13T12:17:03.075",
                    "modifiedDate": "2020-11-13T12:17:03.075"
                },
                {
                    "id": 2,
                    "name": "역삼역",
                    "createdDate": "2020-11-13T12:17:03.092",
                    "modifiedDate": "2020-11-13T12:17:03.092"
                }
            ],
            "createdDate": "2020-11-13T09:11:51.997",
            "modifiedDate": "2020-11-13T09:11:51.997"
        }
        ```

##### 1.1.1.2. 요구사항 설명

###### 1.1.1.2.1. 노선 생성 시 두 종점역 추가하기

인수 테스트와 DTO 등 수정이 필요함

```java
public class LineRequest {
    private String name;
    private String color;
    private Long upStationId;       // 추가
    private Long downStationId;     // 추가
    private int distance;           // 추가
    ...
}
```

###### 1.1.1.2.2. 노선 객체에서 구간 정보를 관리하기

노선 생성시 전달되는 두 종점역은 노선의 상태로 관리되는 것이 아니라 구간으로 관리되어야 함

```java
public class Line {
    ...
    private List<Section> sections;
    ...
}
```

###### 1.1.1.2.3. 노선의 역 목록을 조회하는 기능 구현하기

- 노선 조회 시 역 목록을 함께 응답할 수 있도록 변경
- 노선에 등록된 구간을 순서대로 정렬하여 상행 종점부터 하행 종점까지 목록을 응답하기
- 필요시 노선과 구간(혹은 역)의 관계를 새로 맺기

#### 1.1.2. 힌트

##### 1.1.2.1. 기능 변경 시 인수 테스트를 먼저 변경하기

- 기능(혹은 스펙) 변경 시 테스트가 있는 환경에서 프로덕션 코드를 먼저 수정할 경우 어려움을 겪을 수 있음
    - 프로덕션 코드를 수정하고 그에 맞춰 테스트 코드를 수정해 주어야 해서 두번 작업하는 느낌
- 항상 테스트를 먼저 수정한 다음 프로덕션을 수정하자!
- 더 좋은 방법은 기존 테스트는 두고 새로운 테스트를 먼저 만들고 시작하자!

### 1.2. 기능 요구사항 정리

|구분 | 상세 |구현방법     |
|:----:  |:------  |:---------|
|노선 관리|• 지하철 노선 생성 수정|• `LineAcceptanceTest` 작성<br>• 상행역 추가<br>• 하행역 추가<br>• 간격 추가|
|노선 관리|• 지하철 노선 목록 조회|• `LineAcceptanceTest` 작성<br>• 상행역과 하행역 비교 추가|
|노선 관리|• 지하철 노선 조회|• `LineAcceptanceTest` 작성<br>• 상행역과 하행역 비교 추가|

### 1.3. 프로그래밍 요구사항

|구분|상세|구현 방법|
|:---:|:---|---|
|Convention|• 자바 코드 컨벤션을 지키면서 프로그래밍한다.<br>&nbsp;&nbsp;• https://naver.github.io/hackday-conventions-java/ <br>&nbsp;&nbsp;• https://google.github.io/styleguide/javaguide.html <br>&nbsp;&nbsp;•  https://myeonguni.tistory.com/1596 |- gradle-editorconfig 적용<br>- gradle-checkstyle 적용<br>- IntelliJ 적용<br>- Github 적용|
|테스트|• 모든 기능을 TDD로 구현해 단위 테스트가 존재해야 한다. 단, UI(System.out, System.in) 로직은 제외<br>&nbsp;&nbsp;• 핵심 로직을 구현하는 코드와 UI를 담당하는 로직을 구분한다.<br>&nbsp;&nbsp;•UI 로직을 InputView, ResultView와 같은 클래스를 추가해 분리한다.|- 핵심 로직 단위테스트|

### 1.4. 비기능 요구사항

|구분 |상세 |구현방법     |
|:----:  |:------  |:---------|
|요구사항|• 기능을 구현하기 전에 README.md 파일에 구현할 기능 목록을 정리해 추가한다.|- 요구사항 정의 정리|
|Convention|• git의 commit 단위는 앞 단계에서 README.md 파일에 정리한 기능 목록 단위로 추가한다.<br>&nbsp;&nbsp;• 참고문서 : [AngularJS Commit Message Conventions](https://gist.github.com/stephenparish/9941e89d80e2bc58a153)|- git commit 시 해당 convention 적용|

#### 1.4.1. AngularJS Commit Message Conventions 중

- commit message 종류를 다음과 같이 구분

```
feat (feature)
 fix (bug fix)
 docs (documentation)
 style (formatting, missing semi colons, …)
 refactor
 test (when adding missing tests)
 chore (maintain)
 ```

# 1.4.2. editorConfig setting

```
Execution failed for task ':editorconfigCheck'.
> There are .editorconfig violations. You may want to run

* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.
```

- 위와 같은 에러가 뜨면 다음을 실행한다. `./gradlew editorconfigFormat`

## 2. 분석 및 설계

### 2.1. 이번 Step 핵심 목표

#### 2.1.1. ATDD

> 학습 내용 간단히 정리 [[Markdown 보기]](./summary.md)

### 2.2. Todo List

- [x] 0.기본 세팅
    - [x] 0-1.git fork/clone
        - [x] 0-1-1.NEXTSTEP 내 과제로 이동 및 '미션시작'
        - [x] 0-1-2.실습 github으로 이동
        - [x] 0-1-3.branch 'gregolee'로 변경
        - [x] 0-1-4.fork
        - [x] 0-1-5.clone : `git clone -b gregolee --single-branch https://github.com/gregolee/atdd-subway-admin.git`
        - [x] 0-1-6.branch : `git checkout -b step1`
    - [x] 0-2.요구사항 정리
    - [x] 0-3.[AngularJS Commit Message Conventions](https://gist.github.com/stephenparish/9941e89d80e2bc58a153#generating-changelogmd) 참고
    - [x] 0-4.Slack을 통해 merge가 되는지 확인한 후에 코드 리뷰 2단계 과정으로 다음 단계 미션을 진행
        - [x] 0-4-1.gregolee(master) branch로 체크아웃 : `git checkout gregolee`
        - [x] 0-4-2.step1 branch 삭제 : `git branch -D step1`
        - [x] 0-4-3.step1 branch 삭제 확인 : `git branch -a`
        - [x] 0-4-4.원본(next-step) git repository를 remote로 연결 (미션 당 1회) : `git remote add -t gregolee upstream https://github.com/next-step/atdd-subway-admin`
        - [x] 0-4-5.원본(next-step) git repository를 remote로 연결 확인 : `git remote -v`
        - [x] 0-4-6.원본(next-step) git repository에서 merge된 나의 branch(gregolee)를 fetch : `git fetch upstream gregolee`
        - [x] 0-4-7.remote에서 가져온 나의 branch로 rebase : `git rebase upstream/gregolee`
        - [x] 0-4-7.gregolee -> step2로 체크아웃 : `git checkout -b step2`
    - [x] 0-5.리뷰어님의 리뷰를 반영한 코드로 수정
        - [x] 0-5-1.강의 자료 학습한 부분만 수정
        - [x] 0-5-2.`body`를 `LineRequest`로 이용하여 요청
        - [x] 0-5-3.ATDD 네이밍 변경
            - [x] 0-5-3-1.파라메터 명칭 제거토록
            - [x] 0-5-3-2.given, when, then에 해당하는 명칭으로 변경
            - [x] 0-5-3-3.지하철 노선 생성이라는 말보단 지하철_노선_등록되어_있음등으로 수정
        - [x] 0-5-4.ATDD 값 검증 추가
        - [x] 0-5-5.필요없는 `@Transactional`제거
        - [x] 0-5-6.`LineService.java` 내 찾지 못했을 경우 Custom Exception 발생
        - [x] 0-5-7.`@ExceptionHandler` 중복 제거
        - [x] 0-5-8.`validate()` 생성자에 포함하도록 변경
        - [x] 0-5-9.`LineService.java` : `LineResponse.ofList()` 를 이용해 캡슐화
- [x] 1.자바 코드 컨벤션을 위한 세팅
    - [x] 1-1.[gradle-editorconfig](https://naver.github.io/hackday-conventions-java/#editorconfig) 적용
    - [x] 1-2.[gradle-checkstyle](https://naver.github.io/hackday-conventions-java/#checkstyle) 적용
    - [x] 1-3.[IntelliJ](https://naver.github.io/hackday-conventions-java/#_intellij) 적용
    - [x] 1-4.[Github](https://naver.github.io/hackday-conventions-java/#_github) 적용
- [x] 2.학습
    - [x] 2-1.RestAssured 학습 : [[Usage Guide]](https://github.com/rest-assured/rest-assured/wiki/Usage#examples)
    - [x] 2-2.LiveTemplates - IntelliJ
- [x] 3.분석 및 설계
    - [x] 3-1.step02.md 초안 작성
    - [x] 3-2.ATDD 작성
- [x] 4.구현
    - [x] 4-1.Scenario: 지하철 노선을 생성한다.
        - [x] 4-1-1.`LineAcceptanceTest` 수정
        - [x] 4-1-2.`LineServiceTest` 수정
        - [x] 4-1-3.`StationServiceTest` 수정
        - [x] 4-1-4.`LineRepositoryTest` 수정
        - [x] 4-1-5.`LineTest` 수정
    - [x] 4-2.Scenario: 지하철 노선을 등록할 경우 상행역의 아이디를 입력하지 않으면 지하철 노선을 생성할 수 없다.
    - [x] 4-3.Scenario: 지하철 노선을 등록할 경우 하행역의 아이디를 입력하지 않으면 지하철 노선을 생성할 수 없다.
    - [x] 4-4.Scenario: 지하철 노선을 등록할 경우 간격을 입력하지 않으면 지하철 노선을 생성할 수 없다.
        - [x] 4-4-1.`LineAcceptanceTest` 수정
        - [x] 4-4-2.`LineServiceTest` 수정
        - [x] 4-4-3.`LineRepositoryTest` 수정
        - [x] 4-4-4.`DistanceTest` 수정
        - [x] 4-4-5.`SectionTest` 수정
        - [x] 4-4-5.`SectionRepositoryTest` 수정
        - [x] 4-4-5.`SectionServiceTest` 수정
    - [x] 4-5.Scenario: 지하철 노선을 등록할 경우 간격을 0이하의 숫자로 입력하면 지하철 노선을 생성할 수 없다.
    - [x] 4-6.Scenario: 지하철 노선을 등록할 경우 상행역과 하행역의 아이디가 같으면 지하철 노선을 생성할 수 없다.
    - [x] 4-7.Scenario: 지하철 노선 목록을 조회한다.
        - [x] 4-7-1.`LineServiceTest` 수정
    - [x] 4-8.리뷰어님 코멘트 반영
        - [x] 4-8-1.강의 자료 이미지 제거
        - [x] 4-8-2.HTTP Status NotFound로 변경
        - [x] 4-8-3.`SectionGroup`생성
        - [x] 4-8-4.`Line`엔티티의 필드로 `SectionGroup` 추가
        - [x] 4-8-5.`Line`엔티티의 필드 중 `StationGroup` 제거
        - [x] 4-8-6.`LineService`
            - [x] 4-8-6-1.간결하게 하기
        - [x] 4-8-7.구간을 순서대로 정렬하여 상행 종점부터 하행 종점까지 목록을 응답하기
        - [x] 4-8-8.연관 관계 매핑 변경하기: `SectionGroup` 내 `Station` `@OneToOne` -> `@ManyToOne`
        - [x] 4-8-9.`Distance`: `double` -> `int`
        - [x] 4-8-10.`Distance.java`: 검증 메서드 중복 제거
- [x] 5.테스트
    - [x] 5-1.Gradle build Success 확인
    - [x] 5-2.checkstyle 문제없는지 확인 (Java Convention)
    - [x] 5-3.요구사항 조건들 충족했는지 확인
        - [x] 5-3-1.핵심 단위 로직 테스트 
    - [x] 5-4.인수 테스트 확인
    - [x] 5-5.UI 테스트 확인
- [x] 6.인수인계
    - [x] 6-1.소감 및 피드백 정리
        - [x] 6-1-1.느낀점 & 배운점 작성
        - [x] 6-1-2.피드백 요청 정리
    - [x] 6-2.코드리뷰 요청 및 피드백
        - [x] 6-1-1.step2를 gregolee/atdd-subway-admin로 push : `git push origin step2`
        - [x] 6-1-2.pull request(PR) 작성
    - [x] 6-3.Slack을 통해 merge가 되는지 확인한 후에 미션 종료

### 2.3. ATDD 작성

ATDD 작성 [Markdown 보기](./atdd.md)

## 3. 인수인계

### 3.1. 느낀점 & 배운점

#### 3.1.1. 느낀점

- 하향식 ATDD
    - 도메인을 모른다는 전제로 접근하는데, 정말 어려웠습니다.
    - ATDD 리팩토링 순서 : acceptanceTest -> ServiceTest -> RepositoryTest -> DomainTest
    - 도메인의 테스트를 누락하는 경우가 생기게끔 프로덕션 코드를 수정하는 경우가 발생해서 TDD가 제대로 진행되지 않았습니다.
    - TDD 후 하향식으로 수정하는 연습이 많이 필요할 것 같습니다.
- 데이터 관리 지향 && 절차 지향에 익숙해져버린 프로그래밍
    - 객체 지향 프로그래밍을 위해서는 DTO, Entity의 역할 구분을 확실히 해줘야합니다.
- 객체지향은 기술이 아니라 도메인의 대한 정확한 고찰으로부터 시작된다는 점을 깨달았습니다.    

#### 3.1.2. 배운점

- 하향식, 상향식을 고집할 필요는 없다.
    - 경우에 따라 유동적으로 하향식, 상향식을 번갈아 해야한다.
- 데이터를 어떻게 저장하느냐를 집중하는 것이 아니라 객체(도메인)에 가장 잘 어울릴 방식을 고민한다.
    - DTO : Request, Response에 집중한 객체 방식이다. 도메인의 필드와 일치할 필요가 없다.
    - Entity : 객체지향적으로 생각하여 작성한다. 데이터 지향적인 생각이 들어가지 않도록 해야한다.

### 3.2. 피드백 요청

- 피드백 요청 드릴 사항은 없습니다.