<%--
  Created by IntelliJ IDEA.
  User: admin
  Date: 2024-10-18
  Time: 오후 1:17
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>처리 중...</title>
    <style>
        .progress-bar-container {
            width: 100%;
            background-color: #f3f3f3;
            border: 1px solid #ccc;
            margin-top: 20px;
        }
        .progress-bar {
            width: 0%;
            height: 30px;
            background-color: #4caf50;
            text-align: center;
            color: white;
            line-height: 30px;
        }
    </style>
    <script type="text/javascript">
        document.addEventListener('DOMContentLoaded', function() {
            let retryCount = 0;
            const maxRetries = 5;  // 재시도 최대 횟수
            const retryInterval = 5000;  // 재시도 간격 (5초)

            function checkStatus() {
                fetch('/video-status')
                    .then(response => response.json())
                    .then(data => {
                        console.log("받은 데이터:", data);  // 받은 데이터 전체 출력
                        console.log("진행률:", data.progress);  // 진행률 확인

                        var progressBar = document.querySelector('.progress-bar');
                        progressBar.style.width = data.progress + '%';
                        progressBar.textContent = data.progress + '%';

                        if (data.status === 'completed') {
                            console.log("동영상 처리가 완료되었습니다.");
                            window.location.href = 'openCV/result'; // 처리가 완료되면 결과 페이지로 이동
                        } else if (data.status === 'processing') {
                            retryCount = 0;  // 재시도 횟수 초기화 (정상 통신 시)
                            setTimeout(checkStatus, retryInterval); // 5초마다 상태 체크
                            console.log("아직 동영상이 처리 중입니다.");
                        } else if (data.status === 'error') {
                            console.log("Flask 측에서 동영상 처리에 실패하였습니다.")
                            window.location.href = 'openCV/result';
                        }
                    })
                    .catch(error => {
                        console.error('상태 확인 오류:', error);
                        retryCount++;  // 재시도 횟수 증가

                        if (retryCount < maxRetries) {
                            console.log(`연결이 끊겼습니다. 재시도 중... (${retryCount}/${maxRetries})`);
                            setTimeout(checkStatus, retryInterval);  // 일정 시간 후 재연결 시도
                        } else {
                            console.error('최대 재시도 횟수를 초과했습니다. 다시 시도해 주세요.');
                            alert('서버와의 연결이 불안정합니다. 다시 시도해 주세요.');
                        }
                    });
            }

            checkStatus(); // 상태 체크 시작
        });
    </script>
</head>
<body>
<h1>동영상 처리 중입니다...</h1>
<p>잠시만 기다려 주세요.</p>

<!-- 진행률 바 -->
<div class="progress-bar-container">
    <div class="progress-bar">0%</div>
</div>

</body>
</html>
