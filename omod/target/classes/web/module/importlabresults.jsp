<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="cancerscreeninglabresults.import" /></h2>

<br/>
<table>
  <tr>
   <th>User Id</th>
   <th>Username</th>
  </tr>
  <c:forEach var="user" items="${userList}">
      <tr>
        <td>${user.userId}</td>
        <td>${user.systemId}</td>
        <td>${user.username}</td>
      </tr>
  </c:forEach>
</table>

<%@ include file="/WEB-INF/template/footer.jsp"%>