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

    <!-- 경기일정 노출 -->
    <div class="gameSchedule-list-container">
        <ul id="gameSchedule-list">
            <c:forEach var="gameSchedule" items="${gameSchedules}">
                <li>
                    <span class="matchTime">${gameSchedule.startDate}</span><br>
                    <span>리그: ${gameSchedule.leagueName}</span><br>
                    <span>
                        <span class="home-label">홈</span> <!-- 홈팀 레이블 추가 -->
                    ${gameSchedule.homeTeam}
                    <c:if test="${gameSchedule.homeTeamScore != '' && gameSchedule.awayTeamScore != ''}">
                        <c:set var="homeScoreClass" value="${gameSchedule.homeTeamScore > gameSchedule.awayTeamScore ? 'high-score' : 'low-score'}"/>
                        <c:set var="awayScoreClass" value="${gameSchedule.awayTeamScore > gameSchedule.homeTeamScore ? 'high-score' : 'low-score'}"/>
                        <span class="score ${homeScoreClass}">${gameSchedule.homeTeamScore}</span> :
                        <span class="score ${awayScoreClass}">${gameSchedule.awayTeamScore}</span>
                    </c:if>
                    <c:if test="${gameSchedule.homeTeamScore == '' || gameSchedule.awayTeamScore == ''}">
                        vs
                    </c:if>
                    ${gameSchedule.awayTeam}
                </span>
                </li>
            </c:forEach>
        </ul>
    </div>


</div>

</body>
</html>