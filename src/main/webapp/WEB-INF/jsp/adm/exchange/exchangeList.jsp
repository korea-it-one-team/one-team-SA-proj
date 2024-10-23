<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>

<!-- Tailwind와 나중에 로드 -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/tailwindcss/2.1.4/tailwind.min.css">

<!-- jQuery와 Bootstrap JS 로드 -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<!-- Bootstrap 먼저 로드 -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>





<head>
    <title>교환 신청 목록</title>
</head>
<style>
    /* 모달이 항상 보이도록 강제 스타일 적용 */
    .modal.show {
        display: block !important;
        opacity: 1 !important;
    }
</style>

<body>

<div class="container mt-5">
    <h1 class="text-center mb-4">교환 신청 목록</h1>
    <form method="GET" action="../exchange/list" class="row g-3 mb-4">
        <div class="col-md-4">
            <input type="text" name="search" class="form-control" placeholder="닉네임 또는 상품 검색">
        </div>
        <div class="col-md-4">
            <select name="status" class="form-select">
                <option value="">전체 상태</option>
                <option value="REQUESTED">대기 중</option>
                <option value="COMPLETED">완료</option>
            </select>
        </div>
        <div class="col-md-4">
            <button type="submit" class="btn btn-primary w-100">검색</button>
        </div>
    </form>

    <table class="table table-hover text-center">
        <thead class="table-dark">
        <tr>
            <th>번호</th>
            <th>상품명</th>
            <th>신청자</th>
            <th>상태</th>
            <th>신청 날짜</th>
            <th>처리</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="exchange" items="${exchangeList}">
            <tr>
                <td>${exchange.id}</td>
                <td>${exchange.gifticon_Name}</td>
                <td>${exchange.nickname}</td>
                <td>
                    <span class="badge
                        ${exchange.exchange_status == 'REQUESTED' ? 'bg-warning' :
                          exchange.exchange_status == 'COMPLETED' ? 'bg-success' : 'bg-secondary'}">
                            ${exchange.exchange_status}
                    </span>
                </td>
                <td>${exchange.exchange_date}</td>
                <td>
                    <!-- 상세보기 버튼 클릭 시 모달 열기 -->
                    <button class="btn btn-sm btn-outline-primary"
                            onclick="openModal(${exchange.id})">
                        상세 보기
                    </button>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<!-- 모달 -->
<div class="modal fade" id="exchangeModal" data-bs-backdrop="true" data-bs-keyboard="true"
     tabindex="-1" aria-labelledby="exchangeModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="exchangeModalLabel">교환 신청 상세</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <p><strong>상품명:</strong> <span id="gifticonName"></span></p>
                <p><strong>신청자:</strong> <span id="name"></span></p>
                <p><strong>전화번호:</strong> <span id="phone"></span> <button id="applicationButton" class="btn btn-outline-secondary" onclick="postgifticon()"> </button></p>
            </div>
            <div class="modal-footer">
                <button id="processButton" type="button" class="btn btn-success" onclick="completeExchange()">
                </button>
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
            </div>
        </div>
    </div>
</div>


<script>
    let currentExchangeId;
    let exchangeModal; // 모달 인스턴스 변수 선언
    let currentExchangeStatus; // 현재 상태 저장 변수

    // 모달 열기 및 데이터 로드
    function openModal(id) {
        currentExchangeId = id;
        const currentExchangeUrl = `../exchange/detail?id=` + id;

        $.ajax({
            url: currentExchangeUrl, // 교환 신청 상세 정보 가져오기
            method: 'GET',
            success: function(data) {
                $('#gifticonName').text(data.gifticon_Name);
                $('#name').text(data.member_name);
                $('#phone').text(data.phone);

                // 현재 교환 상태 저장
                currentExchangeStatus = data.exchange_Status;

                // 버튼 텍스트 동적으로 설정
                const processButton = $('#processButton');
                if (currentExchangeStatus === 'COMPLETED') {
                    processButton.text('처리 취소').removeClass('btn-success').addClass('btn-warning');
                } else {
                    processButton.text('처리 완료').removeClass('btn-warning').addClass('btn-success');
                }

                // 문자 전송 버튼 동적 설정
                const applicationButton = $('#applicationButton');
                if (currentExchangeStatus === 'COMPLETED') {
                    applicationButton.text('전송 완료').addClass('btn-secondary').removeClass('btn-outline-secondary').prop('disabled', true);
                } else {
                    applicationButton.text('전송하기').removeClass('btn-secondary').addClass('btn-outline-secondary').prop('disabled', false);
                }

                // Bootstrap 모달 인스턴스 초기화 및 열기
                exchangeModal = new bootstrap.Modal(document.getElementById('exchangeModal'));
                exchangeModal.show();
            },
            error: function() {
                alert('상세 정보를 불러오는데 실패했습니다.');
            }
        });
    }

    // 처리 완료 버튼 클릭 시 상태 업데이트
    function completeExchange() {
        const currentExchangeurl = `../exchange/` + currentExchangeId + `/complete`;
        $.ajax({
            url: currentExchangeurl,  // 상태 업데이트
            method: 'POST',
            success: function() {
                if (currentExchangeStatus == 'COMPLETED') {
                    alert('처리가 취소되었습니다.');
                }else {
                    alert('처리가 완료되었습니다.');
                }
                location.reload();  // 페이지 새로고침
            },
            error: function() {
                alert('처리하는 데 실패했습니다.');
            }
        });
    }

    function postgifticon() {
        if (confirm("정말 전송하시겠습니까?")) {
            const currentExchangeurl = `../exchange/` + currentExchangeId + `/application1`;
            $.ajax({
                url: currentExchangeurl,
                method: 'POST',
                success: function () {
                    alert('문자전송에 성공하였습니다.');
                    $('#applicationButton').text('전송 완료').addClass('btn-secondary').removeClass('btn-outline-secondary').prop('disabled', true);
                },
                error: function () {
                    alert('문자전송 실패');
                }
            });

        }else{
            alert("취소하셨습니다..");
        }
    }
</script>

</body>
</html>
