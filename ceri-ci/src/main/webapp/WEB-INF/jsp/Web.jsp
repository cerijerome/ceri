<%@ page import="ceri.ci.servlet.WebModel" %>
<%@ page import="ceri.ci.servlet.WebParams" %>
<% WebModel model = (WebModel) request.getAttribute("model"); %>
<!DOCTYPE html>
<html>
<body>
<h2>model = <%= model.toString() %></h2>
</body>
</html>