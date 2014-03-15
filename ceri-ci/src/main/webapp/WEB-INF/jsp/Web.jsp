<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
  <head>
    <meta name="viewport" content="width=device-width,minimum-scale=1,maximum-scale=1"/>
    <link rel="stylesheet" href="/static/ci.css">
    <script src="/static/ci.js"></script>
  </head>
  <body>
	<c:set var="build" value="${model.builds.bolt}"/>

	<div id="logo">
		<div>
		<div>C.I.</div>
		<div>ALERT</div>
		<div>SYSTEM</div>
		</div>
	</div>
	
	<div id="villains">
		<h1>
			<p>WANTED</p>
			<p>by Jenkins</p>
		</h1>
		<ul>
			<c:forEach var="item" items="${build.villains}">
			<li>${item.job}<br />
			  <img src="/static/img/${item.name}.jpg" />
			</li>
			</c:forEach>
		</ul>
		<p>For breaking the build</p>
	</div>
	<div id="heroes">
		<h1>
			<p>HEROES</p>
			<p>of Jenkins</p>
		</h1>
		<ul>
			<c:forEach var="item" items="${build.heroes}">
			<li>${item.job}<br />
			  <img src="/static/img/${item.name}.jpg" />
			</li>
			</c:forEach>
		</ul>
		<p>By fixing the build</p>
	</div>
    
    
  </body>
</html>