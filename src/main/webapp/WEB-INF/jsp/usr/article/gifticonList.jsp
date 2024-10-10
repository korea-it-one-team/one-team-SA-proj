<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Gifticon List</title>
</head>
<body>
<h1>Gifticon List</h1>
<ul>
    <c:forEach var="gifticon" items="${gifticons}">
        <li>
            <h2>${gifticon.name}</h2>
            <p>${gifticon.description}</p>
            <p>Points: ${gifticon.points}</p>
            <img src="${gifticon.imageUrl}" alt="${gifticon.name}" />
        </li>
    </c:forEach>
</ul>
</body>
</html>
