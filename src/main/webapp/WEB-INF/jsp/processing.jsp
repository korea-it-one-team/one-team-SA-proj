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
                        } else {
                            setTimeout(checkStatus, 3000); // 3초마다 상태 체크
                            console.log("아직 동영상이 처리 중입니다.");
                        }
                    })
                    .catch(error => console.error('상태 확인 오류:', error));
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
