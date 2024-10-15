<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<!-- daisyUI -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/daisyui/4.12.10/full.css" />
<!-- 테일윈드 -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/tailwindcss/2.1.4/tailwind.min.css">
<head>
    <!-- head 태그 안에 Bootstrap 추가 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

    <title>교환 신청 목록</title>
</head>
<body>
<div class="container mt-5">
    <h1 class="text-center mb-4">교환 신청 목록</h1>

    <form method="GET" action="/admin/exchange" class="row g-3 mb-4">
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
                            ${exchange.exchangeStatus == 'REQUESTED' ? 'bg-warning' :
                              exchange.exchangeStatus == 'COMPLETED' ? 'bg-success' :
                              'bg-secondary'}">
                                ${exchange.exchangeStatus}
                        </span>
                </td>
<%--                <td>${exchange.exchangeDate}</td>--%>
                <td>
                    <a href="/admin/exchange/${exchange.id}" class="btn btn-sm btn-outline-primary">상세 보기</a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>
