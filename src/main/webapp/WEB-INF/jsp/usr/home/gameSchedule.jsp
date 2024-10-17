<%--
  Created by IntelliJ IDEA.
  User: admin
  Date: 2024-10-10
  Time: 오후 2:40
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>${pageTitle }</title>
    <link rel="stylesheet" href="/resource/common.css"/>
    <link rel="stylesheet" href="/resource/news.css"/>
    <script src="/resource/common.js" defer="defer"></script>
    <!-- 제이쿼리, UI 추가 -->
    <link rel="stylesheet" href="https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>

    <!-- 폰트어썸 -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
    <!-- 폰트어썸 FREE 아이콘 리스트 : https://fontawesome.com/v5.15/icons?d=gallery&p=2&m=free -->

</head>
<body>
<div class="main-content">
    <div class="main-title">
        <h1 class="main-title-content">경기일정</h1>
    </div>

    <div class="news-list-container">
        <ul id="news-list"></ul>
    </div>

    <div class="loading-spinner" id="loadingSpinner" style="display: none;">
        <div class="spinner"></div>
    </div>
</div>

<script>
    $(document).ready(function () {
        $("#loadingSpinner").show();

        $.ajax({
            url: '/getGameSchedule', // 컨트롤러의 메서드 호출
            type: 'GET',
            dataType: 'json', // JSON 응답 형식
            success: function (matchList) {
                $("#loadingSpinner").hide(); // 로딩 스피너 숨기기
                matchList.forEach(function (match) {
                    $('#news-list').append(`<li>${match.startDate} ${match.stadium} ${match.homeTeam} vs ${match.awayTeam} ${match.homeScore ? match.homeScore : 'N/A'} : ${match.awayScore ? match.awayScore : 'N/A'}</li>`);
                });
            },
            error: function (xhr, status, error) {
                console.error("AJAX 요청 실패:", error);
                $("#loadingSpinner").hide(); // 오류 발생 시 로딩 스피너 숨기기
            }
        });
    });
</script>
</body>
</html>
