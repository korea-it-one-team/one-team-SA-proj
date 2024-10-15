<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
    <!-- head 태그 안에 Bootstrap 추가 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

    <title>교환 신청 상세</title>
</head>
<body>
<div class="container mt-5">
    <h1 class="text-center mb-4">교환 신청 상세</h1>

    <div class="card mb-4">
        <div class="card-body">
            <h5 class="card-title">상품명: ${exchange.gifticonName}</h5>
            <p class="card-text">신청자: ${exchange.userNickname} (${exchange.userEmail})</p>
            <p class="card-text">신청 날짜: ${exchange.exchangeDate}</p>
            <p class="card-text">
                상태:
                <span class="badge
                    ${exchange.exchangeStatus == 'REQUESTED' ? 'bg-warning' :
                      exchange.exchangeStatus == 'COMPLETED' ? 'bg-success' :
                      'bg-secondary'}">
                    ${exchange.exchangeStatus}
                </span>
            </p>
        </div>
    </div>

    <form method="POST" action="/admin/exchange/${exchange.id}/update" class="row g-3">
        <div class="col-md-6">
            <label for="status" class="form-label">상태 변경</label>
            <select name="status" id="status" class="form-select">
                <option value="REQUESTED" ${exchange.exchangeStatus == 'REQUESTED' ? 'selected' : ''}>대기 중</option>
                <option value="COMPLETED" ${exchange.exchangeStatus == 'COMPLETED' ? 'selected' : ''}>완료</option>
            </select>
        </div>
        <div class="col-md-6">
            <label for="comment" class="form-label">코멘트</label>
            <textarea name="comment" id="comment" class="form-control" rows="3"></textarea>
        </div>
        <div class="col-12">
            <button type="submit" class="btn btn-primary w-100">상태 변경</button>
        </div>
    </form>

    <a href="/admin/exchange" class="btn btn-secondary mt-3">목록으로 돌아가기</a>
</div>
</body>
</html>
