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
    <link rel="stylesheet" href="/resource/gameSchedule.css"/>
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

    <!-- 경기일정 노출 -->
    <div class="gameSchedule-list-container">
        <ul id="gameSchedule-list"></ul>
    </div>

    <!-- 로딩 스피너 노출 -->
    <div class="loading-spinner" id="loadingSpinner" style="display: none;">
        <div class="spinner"></div>
    </div>
</div>

<!-- 경기일정 Ajax 요청 -->
<script>
    $(document).ready(function () {
        $("#loadingSpinner").show();

        $.ajax({
            url: '/getGameSchedule', // 컨트롤러의 메서드 호출
            type: 'GET',
            dataType: 'json', // JSON 응답 형식
            success: function (scheduleMap) {
                $("#loadingSpinner").hide(); // 로딩 스피너 숨기기
                console.log("scheduleMap : ", scheduleMap);

                // 날짜를 오름차순으로 정렬
                var sortedDates = Object.keys(scheduleMap).sort(function (a, b) {
                    return new Date(a) - new Date(b); // 날짜 형식으로 변환 후 비교
                });

                // 정렬된 날짜별로 경기를 출력
                sortedDates.forEach(function (date) {
                    console.log("날짜: " + date + ", 경기에 대한 정보: " + JSON.stringify(scheduleMap[date]));

                    // 날짜 헤더 추가
                    $('#gameSchedule-list').append('<h3 class="date-header">' + date + '</h3>');

                    // 해당 날짜의 리그 정보를 가져와서 출력
                    scheduleMap[date].forEach(function (leagueInfo) {
                        var league = leagueInfo.leagueName; // 리그 이름
                        var matches = leagueInfo.matches; // 경기가 포함된 리스트

                        // 리그 이름에 공백이 있으면 '-' 으로 대체
                        var safeLeague = league.replace(/\s+/g, '-');

                        // 리그별로 경기 정보를 출력
                        $('#gameSchedule-list').append('<h4 class="league-header">' + league + '</h4><div class="match-container" id="date-' + date + '-league-' + safeLeague + '"></div>');

                        matches.forEach(function (match) {
                            var startDate = match.startDate; // 전체 시작 시간 문자열
                            var matchTime = startDate.split('\n')[1]; // ex) "경기 시간 01:45"에서 "01:45" 추출
                            var homeTeam = match.homeTeam;
                            var awayTeam = match.awayTeam;
                            var homeTeamScore = match.homeTeamScore ? match.homeTeamScore : '';
                            var awayTeamScore = match.awayTeamScore ? match.awayTeamScore : '';

                            // 경기 정보를 가로로 배치
                            var matchHTML;
                            if (homeTeamScore || awayTeamScore) {
                                // 스코어가 하나라도 있으면 표시
                                var homeScoreClass = homeTeamScore > awayTeamScore ? 'high-score' : 'low-score';
                                var awayScoreClass = awayTeamScore > homeTeamScore ? 'high-score' : 'low-score';

                                matchHTML = '<li>' +
                                    '<span class ="matchTime">'+ matchTime + '</span>' + '<br>' + // 시간 표시
                                    homeTeam + '<span class="home-label">홈</span> ' +
                                    '<span class="score ' + homeScoreClass + '">' + (homeTeamScore ? homeTeamScore : '') + '</span>' +
                                    ' : ' +
                                    '<span class="score ' + awayScoreClass + '">' + (awayTeamScore ? awayTeamScore : '') + '</span> ' +
                                    awayTeam + '<br>' + '</li>';
                            } else {
                                matchHTML = '<li>' +
                                    '<span class ="matchTime">'+ matchTime + '</span>' + '<br>' + // 시간 표시
                                    homeTeam + '<span class="home-label">홈</span> ' + 'vs ' +
                                    awayTeam + '<br>' + '</li>'; // 점수가 없는 경우 팀 이름만 표시
                            }

                            // 해당 리그의 match-container에 경기 정보 추가
                            $('#date-' + date + '-league-' + safeLeague).append(matchHTML);
                        });
                    });
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