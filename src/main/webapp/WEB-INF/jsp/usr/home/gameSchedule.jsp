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

    <!-- 날짜 선택 추가 -->
    <div class="date-picker-container">
        <label for="datepicker">날짜 선택:</label>
        <input type="text" id="datepicker" readonly>
    </div>

    <div class="gameSchedule-list-container">
        <ul id="gameSchedule-list">
        </ul>
    </div>
</div>

<script>
    $(function () {
        // 현재 날짜 가져오기
        const today = new Date();
        const formattedDate = today.toISOString().split('T')[0]; // 'YYYY-MM-DD' 형식으로 변환
        $("#datepicker").val(formattedDate); // 기본값으로 설정

        // Datepicker 초기화
        $("#datepicker").datepicker({
            dateFormat: "yy-mm-dd",
            defaultDate: today, // 기본 날짜 설정
            onSelect: function (selectedDate) {
                loadGameSchedules(selectedDate); // 선택한 날짜의 경기 일정 로드
            }
        });

        // 기본 로드 시 현재 날짜의 경기 일정 로드
        loadGameSchedules(formattedDate);

        // AJAX 요청 함수
        function loadGameSchedules(selectedDate) {
            $.ajax({
                type: "GET",
                url: "/usr/home/gameSchedule/date",
                data: { date: selectedDate },
                success: function (response) {
                    // 응답이 map인지 확인
                    if (response) {
                        $("#gameSchedule-list").empty(); // 기존 경기 일정 초기화

                        let currentDate = ""; // 현재 날짜 초기화
                        let currentLeague = ""; // 현재 리그 초기화

                        // 각 경기 일정 처리
                        response.schedules.forEach(function (gameSchedule) {
                            const loginedMember = response.loginedMember;
                            const userPredictionsMap = response.userPredictionsMap;

                            // 경기 시작일과 시간
                            const gameStartLocalDateTime = gameSchedule.startDate + "T" + gameSchedule.matchTime;
                            const isFutureGame = new Date() < new Date(gameStartLocalDateTime);

                            // 선택한 날짜와 같은 경기만 표시
                            if (selectedDate === gameSchedule.startDate) {
                                // 날짜 표시
                                if (currentDate !== gameSchedule.startDate) {
                                    currentDate = gameSchedule.startDate;
                                    $("#gameSchedule-list").append(`<h3 class="date-header">` + currentDate + `</h3>`);
                                }

                                // 리그 이름 표시
                                if (currentLeague !== gameSchedule.leagueName) {
                                    currentLeague = gameSchedule.leagueName;
                                    $("#gameSchedule-list").append(`<h4 class="league-header">` + currentLeague + `</h4><div class="league-block"><div class="match-container">`);
                                }

                                // 경기 정보 추가
                                let listItem = createMatchItem(gameSchedule, isFutureGame, loginedMember, userPredictionsMap);
                                $("#gameSchedule-list").append(listItem);
                            }
                        });

                        // 경기 끝나고 league-block 닫기
                        $("#gameSchedule-list").append("</div></div>");
                    } else {
                        alert("잘못된 응답 형식입니다."); // 배열이 아닌 경우 경고
                    }
                },
                error: function (xhr) {
                    alert("경기 일정을 불러오는 중 문제가 발생했습니다.");
                }
            });
        }

        // 경기 정보를 리스트 아이템으로 생성하는 함수
        function createMatchItem(gameSchedule, isFutureGame, loginedMember, userPredictionsMap) {
            let listItem = $(`<li class="match-item"></li>`)
                .append(`<span class="matchTime">` + gameSchedule.matchTime + `</span>`)
                .append(`<div class="home-label" style="margin: 0 5px;">홈</div>`) // 홈 라벨
                .append(`<span style="margin-right: 5px;">` + gameSchedule.homeTeam + `</span>`);

            // 점수 표시
            if (gameSchedule.homeTeamScore !== '' && gameSchedule.awayTeamScore !== '') {
                let homeScoreClass = gameSchedule.homeTeamScore > gameSchedule.awayTeamScore ? 'high-score' : 'low-score';
                let awayScoreClass = gameSchedule.awayTeamScore > gameSchedule.homeTeamScore ? 'high-score' : 'low-score';

                listItem.append(`<span class="score ` + homeScoreClass + `">` + gameSchedule.homeTeamScore + `</span> : <span class="score ` + awayScoreClass + `">` + gameSchedule.awayTeamScore + `</span>`);
            } else {
                // 점수가 없을 경우 'vs' 추가
                listItem.append(`<span> vs </span>`);
            }

            // awayTeam을 추가
            listItem.append(`<span style="margin: 0 5px;">` + gameSchedule.awayTeam + `</span>`);

            // 예측 버튼 처리
            if (loginedMember) {
                let userPrediction = userPredictionsMap ? userPredictionsMap[gameSchedule.id] : null;

                // 예측 버튼을 inline-block으로 설정
                let predictionButtons = $('<div style="display: inline-block;"></div>');
                ['승', '무', '패'].forEach(function (prediction) {
                    let button = $(`<button class="prediction-button" style="margin: 0 2px;" data-prediction="` + prediction + `" data-game-id="` + gameSchedule.id + `" data-member-id="` + loginedMember.id + `"></button>`)
                        .text(prediction)
                        .css("background-color", userPrediction === prediction ? (prediction === '승' ? 'green' : (prediction === '무' ? 'yellow' : 'red')) : 'white');

                    // 미래 경기일 경우 버튼 활성화, 과거 경기일 경우 비활성화
                    if (!isFutureGame) {
                        button.prop('disabled', true);
                    }

                    predictionButtons.append(button);
                });

                listItem.append(predictionButtons);
            }

            return listItem;
        }

        // 예측 버튼 클릭 AJAX 요청
        $('#gameSchedule-list').on('click', '.prediction-button', function (event) {
            event.preventDefault();

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
                    if (response.error) {
                        // 서버에서 오류 메시지를 받은 경우
                        alert(response.error);
                    } else {
                        alert("예측이 성공적으로 저장되었습니다."); // 성공 메시지 추가

                        // 예측한 경기의 날짜를 가져오기 위해 gameSchedule에서 날짜를 찾는 로직 추가
                        const selectedGameSchedule = response.gameSchedule; // 예측된 경기 정보
                        const selectedDate = selectedGameSchedule.startDate; // 예측된 경기의 날짜

                        // 현재 보고 있는 날짜로 경기 일정 다시 로드
                        loadGameSchedules(selectedDate);
                    }
                },
                error: function (xhr) {
                    // 오류 발생 시 서버에서 반환한 메시지를 표시
                    var errorMessage = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : "예측을 저장하는 중 문제가 발생했습니다.";
                    alert(errorMessage);
                }
            });
        });
    });
</script>

</body>
</html>
