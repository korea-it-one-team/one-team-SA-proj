<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Admin Gifticon Management</title>
    <!-- daisyUI -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/daisyui/4.12.10/full.css" />
    <!-- Tailwind -->
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
            justify-content: center;
            gap: 20px;
            max-width: 1270px;
            margin: 0 auto;
        }
        .gifticon-item {
            padding: 15px;
            width: 300px;
            text-align: center;
            border-radius: 8px;
            transition: transform 0.2s;
        }
        .gifticon-item:hover {
            transform: scale(1.05);
        }
        img {
            max-width: 100%;
            border-radius: 4px;
        }
        .header {
            margin: 10px;
            font-size: 4rem;
            font-weight: bold;
        }
    </style>
</head>
<body>
<div class="header">Admin Gifticon Management</div>

<div class="gifticon-list">
    <c:forEach var="gifticon" items="${gifticons}">
        <div class="card card-compact bg-base-100 w-96 shadow-xl gifticon-item">
            <figure>
                <img src="${gifticon.image_url}" alt="${gifticon.name}" />
            </figure>
            <div class="card-body">
                <h2 class="card-title">${gifticon.name}</h2>
                <p class="text-left font-semibold text-xl">Stock: ${gifticon.stock}</p>
                <div class="card-actions justify-end">
                    <button class="btn btn-primary" onclick="openUploadModal(${gifticon.id})">재고 추가</button>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

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

<!-- 모달: 재고 추가 및 파일 업로드 -->
<div id="uploadModal" class="modal">
    <div class="modal-box">
        <h3 class="font-bold text-lg">재고 추가 및 이미지 업로드</h3>
        <form id="uploadForm" enctype="multipart/form-data">
            <input type="hidden" id="gifticonId" name="gifticonId">
            <div class="form-control">
                <label class="label">이미지 파일</label>
                <input type="file" name="imageFile" class="input input-bordered" accept="image/*" multiple required
                       onchange="updateStockCount(this)">
            </div>
            <div class="form-control">
                <label class="label">재고 수량</label>
                <input type="number" name="stock"  id="stockCount" class="input input-bordered" min="1" readonly>
            </div>
            <div class="modal-action">
                <button type="submit" class="btn btn-primary">저장</button>
                <button type="button" class="btn" onclick="closeUploadModal()">취소</button>
            </div>
        </form>
    </div>
</div>

<script>
    let getGifticonId
    // 모달 열기

    function openUploadModal(gifticonId) {
        getGifticonId = gifticonId;
        $('#gifticonId').val(gifticonId);
        $('#uploadModal').addClass('modal-open');
    }

    // 모달 닫기
    function closeUploadModal() {
        $('#uploadModal').removeClass('modal-open');
        $('#uploadForm')[0].reset();
    }

    // 폼 제출: 재고 및 파일 업로드
    $('#uploadForm').submit(function(e) {
        e.preventDefault();
        const formData = new FormData(this);

        $.ajax({
            url: '/gifticon/' + getGifticonId + '/upload',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: function(response) {
                alert(response.message);
                location.reload();  // 페이지 새로고침
            },
            error: function(xhr) {
                alert(xhr.responseJSON.message);
            }
        });
    });

    function updateStockCount(input) {
        const fileCount = input.files.length;
        $('#stockCount').val(fileCount);  // 선택한 파일 개수로 수량 업데이트
    }
</script>
</body>
</html>
