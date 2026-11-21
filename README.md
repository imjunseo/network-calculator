# Java TCP 네트워크 계산기

클라이언트가 정수 연산 명령을 한 줄 보내면 서버가 상태와 결과를 두 줄로 응답하는 Java 소켓 프로젝트입니다. 서버는 스레드 풀을 사용해 여러 연결을 처리합니다.

## 실행

JDK 17을 기준으로, 저장소 루트에서 컴파일합니다.

```bash
javac -d out Calculator.java ServerConfig.java calculator_client.java calculator_server.java
java -cp out myfirstnetworkapp.calculator_server
```

다른 터미널에서 클라이언트를 실행하고 `ADD 10 20`을 입력하면 `Answer: 30.0`이 표시됩니다.

```bash
java -cp out myfirstnetworkapp.calculator_client
```

기본 서버 주소는 `localhost:1234`입니다. 원격 서버에 연결하려면 클라이언트 실행 폴더에 `server_info.txt`를 만들고 첫 줄에 호스트, 둘째 줄에 포트를 적습니다.

## 프로토콜

요청은 `ADD`, `SUB`, `MUL`, `DIV`와 32비트 정수 두 개를 공백으로 구분합니다. 연결 하나에서 요청 하나를 처리합니다.

```text
ADD 10 20
STATUS: 200
VALUE: 30.0
```

`DIV 10 0`은 400, 알 수 없는 연산이나 잘못된 인자는 401을 반환합니다. 서버는 읽기 제한 시간, 요청 길이 제한, 연결 대기열 상한을 적용합니다. 학습용 평문 TCP 프로토콜이므로 신뢰할 수 있는 환경에서 실행하세요.

## 자동 테스트

JDK 17 이상과 Python 3이 있는 환경에서 실행합니다.

```bash
python -m unittest discover -s tests -v
```

테스트는 서버를 임시 포트에서 실제로 실행합니다. 사칙연산과 정수 범위, 잘못된 요청, 24개 동시 요청, 유휴 연결의 읽기 시간 제한을 확인합니다. 직접 다른 포트에서 시험하려면 서버 실행 명령 끝에 포트 번호를 넣으세요.
