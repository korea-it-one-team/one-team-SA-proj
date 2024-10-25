<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
    <title>Gifticon List</title>
    <!-- daisyUI -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/daisyui/4.12.10/full.css" />
    <!-- 테일윈드 -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/tailwindcss/2.1.4/tailwind.min.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body class="flex flex-col items-center">
<div class="text-4xl font-bold my-4">Gifticon List</div>

<div class="flex flex-wrap justify-center gap-5 max-w-7xl mx-auto">
    <c:forEach var="gifticon" items="${gifticons}">
        <div class="card card-compact bg-base-100 w-72 shadow-xl">
            <figure>
                <img class="w-full h-auto rounded-md" src="${gifticon.image_url}" alt="${gifticon.name}" />
            </figure>
            <div class="card-body">
                <h2 class="card-title text-lg">${gifticon.name}</h2>
                <p class="font-bold text-xl text-left">Points: <span class="points" data-points="${gifticon.points}">${gifticon.points}</span></p>
                <p class="font-bold text-xl text-left">Stocks: ${gifticon.stock} 개</p>
                <div class="card-actions justify-end">
                    <button class="btn btn-primary" onclick="exchangeGifticon('${gifticon.id}','${gifticon.stock}')">교환신청하기</button>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

<!-- 동적 페이징 -->
<div class="flex justify-center mt-6">
    <c:set var="paginationLen" value="3" />
    <c:set var="startPage" value="${page - paginationLen >= 1 ? page - paginationLen : 1}" />
    <c:set var="endPage" value="${page + paginationLen <= pagesCount ? page + paginationLen : pagesCount}" />
    <c:set var="baseUri" value="?searchKeyword=${searchKeyword}" />

    <c:if test="${startPage > 1}">
        <a class="btn btn-sm" href="${baseUri}&page=1">1</a>
    </c:if>
    <c:if test="${startPage > 2}">
        <button class="btn btn-sm btn-disabled">...</button>
    </c:if>

    <c:forEach begin="${startPage}" end="${endPage}" var="i">
        <a class="btn btn-sm ${param.page == i ? 'btn-active' : ''}" href="${baseUri}&page=${i}">${i}</a>
    </c:forEach>

    <c:if test="${endPage < pagesCount - 1}">
        <button class="btn btn-sm btn-disabled">...</button>
    </c:if>

    <c:if test="${endPage < pagesCount}">
        <a class="btn btn-sm" href="${baseUri}&page=${pagesCount}">${pagesCount}</a>
    </c:if>
</div>

<script>
    function exchangeGifticon(id, stock) {
        if (stock <= 0) {
            alert("재고가 없습니다.");
            return false;
        }
        if (confirm("정말 교환하시겠습니까?")) {
            $.ajax({
                url: `gifticons/` + id + `/application`,  // 상태 업데이트
                method: 'POST',
                success: function(response) {
                    alert(response.message);
                    location.reload();  // 페이지 새로고침
                },
                error: function(xhr) {
                    const errorResponse = xhr.responseJSON;
                    alert(errorResponse.message);
                }
            });
        } else {
            alert("취소하셨습니다..");
        }
    }

    // 포인트를 가격 형식으로 변환
    document.querySelectorAll('.points').forEach(function(el) {
        const points = el.dataset.points;
        el.textContent = "Points: " + new Intl.NumberFormat().format(points);
    });
</script>
</body>
</html>
