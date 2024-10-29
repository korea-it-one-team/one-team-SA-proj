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
</head>
<body>
<div class="main-content">
    <div class="main-title">
        <h1 class="main-title-content">경기 일정</h1>
    </div>

    <div class="gameSchedule-list-container">
        <ul id="gameSchedule-list">
            <c:set var="currentDate" value=""/>
            <c:set var="currentLeague" value=""/>

            <c:forEach var="gameSchedule" items="${gameSchedules}">
                <c:if test="${currentDate != gameSchedule.startDate}">
                    <c:set var="currentDate" value="${gameSchedule.startDate}"/>
                    <h3 class="date-header">${gameSchedule.startDate}</h3>
                </c:if>

                <c:if test="${currentLeague != gameSchedule.leagueName}">
                    <c:set var="currentLeague" value="${gameSchedule.leagueName}"/>
                    <h4 class="league-header">${gameSchedule.leagueName}</h4>
                    <div class="league-block">
                    <div class="match-container">
                </c:if>

                <li class="match-item">
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

                    <!-- 예측 폼 추가 (로그인된 사용자만 예측 가능) -->
                    <form action="/predict" method="post">
                        <input type="hidden" name="gameId" value="${schedule.id}"/>
                        <input type="hidden" name="memberId" value="${memberId}"/>
                        <button type="submit" name="prediction" value="승"
                                style="background-color: ${winDrawLose.prediction == '승' ? 'green' : 'white'};">승
                        </button>
                        <button type="submit" name="prediction" value="무"
                                style="background-color: ${winDrawLose.prediction == '무' ? 'yellow' : 'white'};">무
                        </button>
                        <button type="submit" name="prediction" value="패"
                                style="background-color: ${winDrawLose.prediction == '패' ? 'red' : 'white'};">패
                        </button>
                    </form>
                </li>

                <c:if test="${currentLeague != gameSchedule.leagueName || gameSchedule == gameSchedules[gameSchedules.size()-1]}">
                    </div>
                    </div>
                </c:if>
            </c:forEach>
        </ul>
    </div>
</div>

<script>
    $(function () {
        $('.prediction-button').click(function (event) {
            event.preventDefault();

            var prediction = $(this).data('prediction');
            var gameId = $(this).closest('.prediction-form').data('game-id');
            var memberId = $(this).closest('.prediction-form').data('member-id');

            $.post('/predict', {
                gameId: gameId,
                memberId: memberId,
                prediction: prediction
            })
                .done(function (response) {
                    alert("예측이 저장되었습니다!");
                    location.reload();
                })
                .fail(function (error) {
                    console.log("에러 발생:", error);
                    alert("예측을 저장하는 중 문제가 발생했습니다.");
                });
        });
    });
</script>

</body>
</html>
