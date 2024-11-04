<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>${pageTitle }</title>
    <link rel="stylesheet" href="/resource/common.css"/>
    <link rel="stylesheet" href="/resource/gameSchedule.css"/>
    <script src="/resource/common.js" defer="defer"></script>
    <link rel="stylesheet" href="https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
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
                <c:set var="gameStartLocalDate" value="${gameSchedule.startDate}"/>
                <c:set var="gameStartLocalTime" value="${gameSchedule.matchTime}"/>
                <c:set var="gameStartLocalDateTime" value="${gameStartLocalDate}T${gameStartLocalTime}"/>

                <!-- 게임 시작 시간을 비교하기 위한 형식 지정 -->
                <c:set var="isFutureGame" value="${currentDateTime.compareTo(gameStartLocalDateTime) < 0}" />

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

                    <c:if test="${not empty sessionScope.loginedMember}">
                        <c:if test="${isFutureGame}">
                            <!-- userPredictionsMap에서 현재 gameSchedule.id에 맞는 예측 정보 가져오기 -->
                            <c:set var="userPrediction" value="${userPredictionsMap[gameSchedule.id]}"/>

                            <button class="prediction-button" data-prediction="승" data-game-id="${gameSchedule.id}"
                                    data-member-id="${sessionScope.loginedMember.id}"
                                    style="background-color: ${userPrediction == '승' ? 'green' : 'white'};">승
                            </button>
                            <button class="prediction-button" data-prediction="무" data-game-id="${gameSchedule.id}"
                                    data-member-id="${sessionScope.loginedMember.id}"
                                    style="background-color: ${userPrediction == '무' ? 'yellow' : 'white'};">무
                            </button>
                            <button class="prediction-button" data-prediction="패" data-game-id="${gameSchedule.id}"
                                    data-member-id="${sessionScope.loginedMember.id}"
                                    style="background-color: ${userPrediction == '패' ? 'red' : 'white'};">패
                            </button>
                        </c:if>
                        <c:if test="${!isFutureGame}">

                            <!-- userPredictionsMap에서 현재 gameSchedule.id에 맞는 예측 정보 가져오기 -->
                            <c:set var="userPrediction" value="${userPredictionsMap[gameSchedule.id]}"/>

                            <button class="prediction-button" style="background-color: ${userPrediction == '승' ? 'green' : 'white'}; cursor: not-allowed;" disabled>승</button>
                            <button class="prediction-button" style="background-color: ${userPrediction == '무' ? 'yellow' : 'white'}; cursor: not-allowed;" disabled>무</button>
                            <button class="prediction-button" style="background-color: ${userPrediction == '패' ? 'red' : 'white'}; cursor: not-allowed;" disabled>패</button>
                        </c:if>
                    </c:if>
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
            // 예측 값, 게임 ID, 회원 ID를 변수로 저장
            var prediction = $(this).data('prediction');
            var gameId = $(this).data('game-id');
            var memberId = $(this).data('member-id');

            // 각 값이 올바르게 설정되었는지 확인하기 위해 콘솔 로그 추가
            console.log("prediction:", prediction);
            console.log("gameId:", gameId);
            console.log("memberId:", memberId);

            // 빈 값이거나 설정되지 않은 값이 있을 경우 경고창 표시
            if (!gameId || !memberId) {
                alert("게임 ID와 회원 ID는 필수입니다.");
                return;
            }

            // 예측 값을 서버로 전송
            $.ajax({
                type: "POST",
                url: "/predict",
                data: {
                    gameId: gameId,
                    memberId: memberId,
                    prediction: prediction
                },
                success: function (response) {
                    console.log("응답:", response);  // 응답 객체 확인
                    if (response.redirectUrl) {
                        window.location.href = response.redirectUrl; // 리다이렉트
                    } else if (response.error) {
                        // 서버에서 오류 메시지를 받은 경우
                        alert(response.error);
                    } else {
                        alert("예상치 못한 응답이 왔습니다.");
                    }
                },
                error: function (xhr) {
                    // 오류 발생 시 서버에서 반환한 메시지를 표시
                    var errorMessage = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : "예측을 저장하는 중 문제가 발생했습니다.";
                    console.log("에러 발생:", errorMessage);
                    alert(errorMessage);
                }
            });
        });
    });
</script>

</body>
</html>
