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
        <h1 class="main-title-content">경기 일정</h1>
    </div>

    <!-- 경기 일정 노출 -->
    <div class="gameSchedule-list-container">
        <ul id="gameSchedule-list">
            <c:set var="currentDate" value=""/>
            <c:set var="currentLeague" value=""/>

            <c:forEach var="gameSchedule" items="${gameSchedules}">
                <!-- 날짜가 바뀔 때마다 새로운 날짜 헤더 출력 -->
                <c:if test="${currentDate != gameSchedule.startDate}">
                    <c:set var="currentDate" value="${gameSchedule.startDate}"/>
                    <h3 class="date-header">${gameSchedule.startDate}</h3> <!-- 날짜 헤더 스타일 적용 -->
                </c:if>

                <!-- 리그가 바뀔 때마다 새로운 리그 헤더 출력 및 리그별 블록 시작 -->
                <c:if test="${currentLeague != gameSchedule.leagueName}">
                    <c:set var="currentLeague" value="${gameSchedule.leagueName}"/>
                    <h4 class="league-header">${gameSchedule.leagueName}</h4> <!-- 리그 헤더 스타일 적용 -->
                    <!-- 리그별로 게임을 묶을 블록 시작 -->
                    <div class="league-block">
                    <div class="match-container"> <!-- 경기 항목을 담을 컨테이너 -->
                </c:if>

                <!-- 경기 항목 -->
                <li class="match-item">
                    <!-- 경기 시작 시간 -->
                    <span class="matchTime">${gameSchedule.matchTime}</span>
                    <div class="home-label">홈</div>
                    <span>${gameSchedule.homeTeam}</span>
                    <c:if test="${gameSchedule.homeTeamScore != '' && gameSchedule.awayTeamScore != ''}">
                        <c:set var="homeScoreClass"
                               value="${gameSchedule.homeTeamScore > gameSchedule.awayTeamScore ? 'high-score' : 'low-score'}"/>
                        <c:set var="awayScoreClass"
                               value="${gameSchedule.awayTeamScore > gameSchedule.homeTeamScore ? 'high-score' : 'low-score'}"/>
                        <span class="score ${homeScoreClass}">${gameSchedule.homeTeamScore}</span> :
                        <span class="score ${awayScoreClass}">${gameSchedule.awayTeamScore}</span>
                    </c:if>
                    <c:if test="${gameSchedule.homeTeamScore == '' || gameSchedule.awayTeamScore == ''}">
                        vs
                    </c:if>
                    <span>${gameSchedule.awayTeam}</span>
                </li>

                <!-- 리그가 끝날 때 리그 블록 닫기 -->
                <c:if test="${currentLeague != gameSchedule.leagueName || gameSchedule == gameSchedules[gameSchedules.size()-1]}">
                    </div> <!-- 경기 항목 컨테이너 종료 -->
                    </div> <!-- 리그 블록 종료 -->
                </c:if>
            </c:forEach>
        </ul>
    </div>

</div>

</body>
</html>