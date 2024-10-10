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
        <h1 class="main-title-content">금일 KBO 기사</h1>
    </div>

    <!-- 네이버 야구 뉴스 링크 -->
    <div class="total-news">
        <a class="total-news-btn" target="_blank" href="https://sports.news.naver.com/wfootball/news/index?isphoto=N">전체
            뉴스 보기</a>
    </div>

    <!-- 뉴스 리스트를 표시할 요소 -->
    <div class="news-list-container">
        <ul id="news-list"></ul>
    </div>

    <div class="loading-spinner" id="loadingSpinner" style="display: none;">
        <div class="spinner"></div>
    </div>
</div>

<!-- 네이버 뉴스 Ajax 요청 -->
<script>
    $(document).ready(function () {
        // 스피너 표시
        $("#loadingSpinner").show();

        // 뉴스 가져오는 함수
        function fetchNews(teamId) {
            // 스피너 표시
            $("#loadingSpinner").show();

            // 뉴스 리스트 초기화
            $('#news-list').empty(); // 기존 뉴스 리스트를 초기화

            // 서버로부터 뉴스 데이터를 불러와서 동적으로 페이지에 삽입
            $.ajax({
                url: "${pageContext.request.contextPath}/getNews",  // 서버의 API 엔드포인트
                method: "GET",  // GET 메서드로 요청
                success: function (data) {
                    // 뉴스 데이터를 리스트로 변환해서 출력
                    let newsHtml = '';
                    $.each(data, function (index, news) {
                        newsHtml += '<li style="display: flex; align-items: flex-start; margin-bottom: 10px;">'; // 리스트 항목에 flex 스타일 추가

                        // 이미지가 빈 문자열이 아닐 경우에만 이미지 추가
                        if (news.imgUrl) {
                            newsHtml += '<img src="' + news.imgUrl + '" alt="' + news.title + '" style="width: 140px; height: auto; margin-right: 10px; object-fit: cover; object-position: center;" />'; // 이미지 너비 140px로 고정, 높이는 자동
                        }

                        newsHtml += '<div style="flex-grow: 1;">'; // 이미지와 설명을 감싸는 div 추가
                        newsHtml += '<h2 style="margin: 0; font-size: 1.2em;"><a href="' + news.link + '" target="_blank">' + news.title + '</a></h2>'; // 제목 스타일 조정
                        newsHtml += '<p style="margin: 5px 0;">' + news.desc + '</p>'; // 설명 추가 및 여백 조정
                        newsHtml += '<span style="font-size: 0.9em; color: #578CE1;">' + news.press + '</span> <span style="color: #E3E3E3;"> | </span> <span style="font-size: 0.9em; color: gray;">' + news.time + '</span>';
                        newsHtml += '</div>'; // div 닫기
                        newsHtml += '</li>'; // 리스트 항목 닫기
                        newsHtml += '<hr style="color:gray; margin-bottom:10px;">'; // 밑줄 추가
                    });
                    // 뉴스 리스트를 HTML로 변환하여 삽입
                    $('#news-list').html(newsHtml);
                },
                error: function () {
                    // 오류 발생 시 메시지를 표시
                    $('#news-list').html('<li>Failed to load news.</li>');
                },
                complete: function () {
                    // 스피너 숨기기
                    $("#loadingSpinner").hide();
                }
            });
        }
    });
</script>


</body>
</html>
