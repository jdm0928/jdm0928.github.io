<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% request.setCharacterEncoding("utf-8"); %>
<!doctype html>
<html>

<head>
    <meta charset="utf-8">
    <meat name="viewport" content="width=device-width, initial-scale=1" />
    <title>정보 확인</title>
</head>

<body>
    <section>
        <h2>JSP</h2>
        <section>
            <h2>입력 정보</h2>
            <h2>입력값:<%= request.getParameter("id_form")<br/></h2>
        </section>
    </section>
</body>

</html>