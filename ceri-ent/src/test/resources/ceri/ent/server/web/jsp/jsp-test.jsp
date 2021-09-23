<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page import = "java.time.*" %>
<!DOCTYPE html>
<html>
<head>
<title>JSP Test</title>
<link rel="stylesheet" href="css/ent-web.css" />
</head>
<body>
  <div>[<% out.print(LocalTime.now()); %>] JSP is working</div>
  <script src="js/jquery.min.js"></script>
</body>
</html>
