<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="../common/head.jspf" %>

<head>
    <title>고객센터</title>

    <script>
        // 섹션 표시 함수
        function showSection(section) {
            $('.content-section').hide(); // 모든 섹션 숨기기
            $('#' + section).show(); // 선택한 섹션 보이기
            $('.faq-list').slideUp();

        }

        // 페이지 로드 시 초기 설정
        $(document).ready(function () {
            showSection('faq'); // 기본적으로 FAQ 섹션 표시
        });
    </script>

    <script>
        $(document).ready(function() {
            // 카테고리 클릭 시 FAQ 토글
            $('.category-title').on('click', function() {
                const faqList = $(this).next('.faq-list');
                $('.faq-list').not(faqList).addClass('hidden');
                faqList.removeClass('hidden');
                $('.faq-answer').slideUp();
            });

            // 질문 클릭 시 답변 표시/숨기기
            $('.faq-question').on('click', function() {
                $(this).next('.faq-answer').slideToggle();
            });
        });
    </script>

    <script>
        $(document).ready(function() {
            // 상담 저장 처리
            $('form').on('submit', function(event) {
                event.preventDefault(); // 기본 제출 방지

                const title = $('#title').val();
                const content = $('#content').val();

                $.ajax({
                    url: '/submit-consultation',
                    method: 'POST',
                    data: { title: title, content: content },
                    success: function(response) {
                        // 상담 내역 업데이트
                        $('#history ul').append(`
                        <li>
                            <a href="#" class="text-blue-500 hover:underline">${response.title}</a>
                            <span class="text-gray-500">답변대기중</span>
                        </li>
                    `);
                        // 상담 내역 섹션 표시
                        showSection('history');
                        // 폼 초기화
                        $('#title').val('');
                        $('#content').val('');
                    },
                    error: function() {
                        alert('상담 저장 중 오류가 발생했습니다.'); // 에러 처리
                    }
                });
            });
        });
    </script>


    <style>
        /* 전체 제목 스타일 */
        .content-section {

            margin: auto; /* 가운데 정렬 */
            padding: 20px; /* 내부 여백 */
            background-color: #f9f9f9; /* 배경색 */
            border-radius: 8px; /* 모서리 둥글게 */
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); /* 그림자 효과 */
        }

        /* 카테고리 제목 스타일 */
        .category-title {
            font-size: 1.5rem; /* 카테고리 제목 크기 */
            padding: 12px 24px; /* 여백 추가 */
            border-radius: 8px; /* 둥근 모서리 */
            transition: background-color 0.3s; /* 배경색 변화 효과 */
            color: #333; /* 글자 색상 */
        }

        .category-title:hover {
            background-color: #e0e0e0; /* 호버 시 배경색 */
        }

        /* 질문 스타일 */
        .faq-question {
            margin: 10px 0; /* 질문 간격 */
            font-size: 1.5rem; /* 질문 크기 */
            font-weight: 600; /* 글자 두께 */
            color: #007bff; /* 질문 글자 색상 */
            transition: color 0.3s; /* 색상 변화 효과 */
        }

        .faq-question:hover {
            color: #0056b3; /* 호버 시 질문 색상 */
        }

        /* 답변 스타일 */
        .faq-answer {
            margin-left: 20px; /* 답변 왼쪽 여백 */
            font-size: 1.2rem; /* 답변 크기 */
            color: #555; /* 답변 색상 */
            font-weight: bold;
            line-height: 1.5; /* 줄 높이 */
        }

    </style>

</head>
<body>
<div class="container mx-auto p-5 bg-white shadow-lg rounded-lg mt-5">
    <h1 class="text-4xl font-bold text-center mb-5">고객센터</h1>

    <div class="flex justify-center space-x-4 mb-5">
        <button class="btn btn-primary" onclick="showSection('faq')">자주 묻는 질문</button>
        <button class="btn btn-primary" onclick="showSection('consultation')">1:1 상담</button>
        <button class="btn btn-primary" onclick="showSection('history')">상담내역</button>
    </div>

<div id="faq" class="content-section">
    <h2 class="text-2xl font-bold mb-6 ">자주 묻는 질문 Q&A</h2>
    <div id="faq-list" class="flex flex-wrap gap-4 mb-4">
        <c:forEach var="category" items="${categorys}">
            <div>
                <h3 class="category-title btn btn-outline" data-category-id="${category.id}">
                    ${category.category_name}
                </h3>
                <div class="faq-list hidden mt-2" data-category-id="${category.id}">
                    <c:forEach var="faq" items="${faqs}">
                        <c:if test="${faq.category_name == category.category_name}">
                            <h4 class="faq-question cursor-pointer" data-faq-id="${faq.id}">
                                ${faq.question}
                            </h4>
                            <div class="faq-answer hidden" data-faq-id="${faq.id}">
                                <p>${faq.answer}</p>
                            </div>
                        </c:if>
                    </c:forEach>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

    <div id="consultation" class="content-section" style="display: none;">
        <h2 class="text-xl font-semibold">1:1 상담</h2>
        <form action="" method="post">
        <div class="mb-4">
                <label for="title" class="block text-sm font-medium">제목:</label>
                <input type="text" class="input input-bordered w-full" id="title" name="title" required>
            </div>
            <div class="mb-4">
                <label for="content" class="block text-sm font-medium">내용:</label>
                <textarea class="textarea textarea-bordered w-full h-80" id="content" name="content" required></textarea>
            </div>
            <button type="submit" class="btn btn-success">저장</button>
        </form>
    </div>


<div id="history" class="content-section" style="display: none;">
    <h2 class="text-xl font-semibold">상담내역</h2>
    <ul>
        <c:forEach var="consultataion" items="${consultataions}">
            <li>
                <a href="#" onclick="showConsultationDetails(${consultataion.id})" class="text-blue-500 hover:underline">
                    ${consultataion.title}
                </a>

                <div class="hidden content_answer${consultataion.id} content_answer">
                <span class="text-gray-500 block">${consultataion.content}</span>

                <span class="text-gray-500 block">${consultataion.answer}</span>
                </div>

            </li>
        </c:forEach>
    </ul>
</div>
</div>

<script>
    function showConsultationDetails(id){
        $('.content_answer').addClass('hidden');
        $('.content_answer' + id).removeClass('hidden');
    }


</script>
</body>
</html>