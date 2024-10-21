<%--
  Created by IntelliJ IDEA.
  User: admin
  Date: 2024-10-18
  Time: 오후 1:18
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>변환된 미디어</title>
    <script type="text/javascript">
        document.addEventListener('DOMContentLoaded', function() {
            var mediaElement = document.querySelector('.media-element');

            if (mediaElement.tagName === 'IMG') {
                mediaElement.onload = function() {
                    console.log('이미지 로드 완료:', mediaElement.src);
                };
                mediaElement.onerror = function(event) {
                    console.error('이미지 로드 실패:', mediaElement.src);
                    console.error('에러 코드:', event);
                };
            } else if (mediaElement.tagName === 'VIDEO') {
                mediaElement.onloadeddata = function() {
                    console.log('동영상 로드 완료:', mediaElement.src);
                };
                mediaElement.onerror = function(event) {
                    console.error('동영상 로드 실패:', mediaElement.src);
                    console.error('에러 코드:', event);
                };
            }
        });
    </script>
</head>
<body>
<h1>변환된 미디어</h1>

<c:choose>
    <c:when test="${not empty imageSrc}">
        <img class="media-element" src="${imageSrc}" alt="변환된 이미지" />
    </c:when>

    <c:when test="${not empty videoSrc}">
        <video class="media-element" controls preload="auto">
            <source src="${videoSrc}" type="video/mp4">
            브라우저에서 동영상을 지원하지 않습니다.
        </video>
    </c:when>

    <c:otherwise>
        <p>미디어 파일이 없습니다.</p>
    </c:otherwise>
</c:choose>

</body>
</html>

