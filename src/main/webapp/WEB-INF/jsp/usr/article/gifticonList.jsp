<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<title>Gifticon List</title>
<!-- daisyUI -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/daisyui/4.12.10/full.css" />
<!-- 테일윈드 -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/tailwindcss/2.1.4/tailwind.min.css">
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>



<style>
    body {
        display: flex;
        flex-direction: column;
        align-items: center;
    }
    .gifticon-list {
        display: flex;
        flex-wrap: wrap;
        justify-content: center; /* 중앙 정렬 */
        gap: 20px;
        max-width: 1270px; /* 최대 너비 설정 */
        margin: 0 auto; /* 가운데 정렬 */
    }
    .gifticon-item {
        padding: 15px;
        width: 300px; /* 너비를 고정하여 크기 조절 */
        box-sizing: border-box;
        text-align: center; /* 텍스트 중앙 정렬 */
        border-radius: 8px; /* 모서리 둥글게 */
        transition: transform 0.2s; /* 호버 효과 */
    }
    .gifticon-item:hover {
        transform: scale(1.05); /* 호버 시 약간 확대 */
    }
    img {
        max-width: 100%; /* 이미지 크기 조절 */
        height: auto; /* 비율 유지 */
        border-radius: 4px; /* 이미지 모서리 둥글게 */
    }
    .pagination {
        margin-top: 20px;
        text-align: center;
    }
    .pagination a {
        margin: 0 5px;
        text-decoration: none;
    }

    .point_price, .stock{
        text-align: left;
        font-weight: bold;
        font-size: 1.5rem;
    }
    .gifticon-name{
        font-size: 1rem;
    }
    .header{
        margin: 10px;
        font-size: 4rem;
        font-weight: bold;
    }

</style>
</head>
<body>
<div class="header">Gifticon List</div>

<div class="gifticon-list">
<c:forEach var="gifticon" items="${gifticons}">
    <div class="card card-compact bg-base-100 w-96 shadow-xl gifticon-item">
        <figure>
            <img src="${gifticon.image_url}" alt="${gifticon.name}" />
        </figure>
        <div class="card-body">
            <h2 class="card-title">${gifticon.name}</h2>
            <p class="point_price points" data-points="${gifticon.points}">Points: ${gifticon.points}</p>
            <p class="stock stocks">Stocks : ${gifticon.stock} 개</p>
            <div class="card-actions justify-end">
                <button class="btn btn-primary" onclick="exchangeGifticon('${gifticon.id}','${gifticon.stock}')">교환신청하기</button>
            </div>
        </div>
    </div>
</c:forEach>
</div>


<%--<div class="gifticon-list">--%>
<%--    <c:forEach var="gifticon" items="${gifticons}">--%>
<%--        <div class="gifticon-item">--%>
<%--            <img src="${gifticon.image_url}" alt="${gifticon.name}" />--%>
<%--            <p class="gifticon-name">${gifticon.name}</p>--%>
<%--            <p class="point_price points" data-points="${gifticon.points}">Points: ${gifticon.points}</p>--%>
<%--            <button class="btn btn-success " onclick="exchangeGifticon('${gifticon.id}')">교환신청하기</button>--%>
<%--        </div>--%>
<%--    </c:forEach>--%>
<%--</div>--%>

<!-- 	동적 페이징 -->
<div class="pagination flex justify-center mt-3">
    <c:set var="paginationLen" value="3" />
    <c:set var="startPage" value="${page -  paginationLen  >= 1 ? page - paginationLen : 1}" />
    <c:set var="endPage" value="${page +  paginationLen  <= pagesCount ? page + paginationLen : pagesCount}" />
    <c:set var="baseUri" value="?searchKeyword=${searchKeyword}" />

    <c:if test="${startPage > 1 }">
        <a class="btn btn-sm" href="${ baseUri}&page=1">1</a>

    </c:if>
    <c:if test="${startPage > 2 }">
        <button class="btn btn-sm btn-disabled">...</button>
    </c:if>

    <c:forEach begin="${startPage }" end="${endPage }" var="i">
        <a class="btn btn-sm ${param.page == i ? 'btn-active' : '' }" href="${ baseUri}&page=${i }">${i }</a>
    </c:forEach>

    <c:if test="${endPage < pagesCount - 1 }">
        <button class="btn btn-sm btn-disabled">...</button>
    </c:if>

    <c:if test="${endPage < pagesCount }">
        <a class="btn btn-sm" href="${ baseUri}&page=${pagesCount }">${pagesCount }</a>
    </c:if>
</div>


<script>
    function exchangeGifticon(id,stock) {
        if (stock <= 0){
            alert("재고가 없습니다.");
            return false;
        }
        if(confirm("정말 교환하시겠습니까?")){

            $.ajax({
                url: `../gifticons/`+ id + `/application`,  // 상태 업데이트
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

        }else{
            alert("취소하셨습니다.. ID: " + id);
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
