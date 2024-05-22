<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="Edit Patients" otherwise="/login.htm" redirect="/module/cancerscreeninglabresults/cancerscreeninglabresults.htm"/>

    <script src="https://code.jquery.com/jquery-3.6.4.min.js">
    </script>
    <script src="//cdn.rawgit.com/rainabba/jquery-table2excel/1.1.0/dist/jquery.table2excel.min.js">
    </script>

<h2><spring:message code="cancerscreeninglabresults.title" /></h2>

<br/>

<br/>
<form method="POST"enctype="multipart/form-data" modelAttribute="UploadedResults"
<openmrs:require privilege="Edit Patients" otherwise="/login.htm" redirect="/module/cancerscreeninglabresults/cancerscreeninglabresults.htm"/>
    <body>
        <table>
          <tr>
            <td>Encounter Date</td>
            <td>
                <input type="date" id="encounterDate" name="encounterDate">
            </td>
          </tr>
          <tr>
            <td>Location</td>
            <td>
                <select name="selectedLocation" id="selectedLocation">
                    <c:forEach var="location" items="${locations}">
                        <option value="${location.locationId}" >${location.name}</option>
                    </c:forEach>
                </select>
            </td>
          </tr>
          <tr>
            <td>Encounter Type</td>
            <td>
                <select name="selectedEncounterType" id="selectedEncounterType">
                    <c:forEach var="encounterType" items="${encounterTypes}">
                        <option value="${encounterType.encounterTypeId}" >${encounterType.name}</option>
                    </c:forEach>
                </select>
            </td>
          </tr>
          <tr>
            <td>Provider</td>
            <td>
                <select name="selectedProvider" id="selectedProvider">
                    <c:forEach var="provider" items="${providers}">
                        <option value="${provider.providerId}" >
                            <c:forEach var="name" items="${provider.person.names}">
                                ${name}
                            </c:forEach>
                        </option>
                    </c:forEach>
                </select>
            </td>
          </tr>
          <tr>
            <td>Upload File </td>
            <td>
                <input id="excelFile" type = "file" name = "excelFile" accept=".csv,.xls,.xlsx" size = "50" />
            </td>
          </tr>
          <tr>
            <td><input id="uploadFile" type = "submit" value = "Upload" /></td>
          </tr>
        </table>
    </body>
</form>
<div id="errorSection" class="error"></div>
<div id="messagesSection"></div>

<br/> <br/> <br/>
        <button id="dwnldBtn" >
                Export to Excel Sheet
        </button>
 <br/>
<table id="unSavedDateTable" border='3'>
    <caption>LIST OF HPV RESULTS NOT SAVED</caption>
    <tr>
        <td>POSITION</td>
        <td>SPECIMEN CODE</td>
        <td>HPV RESULTS</td>
        <td>POSITIVE TYPES</td>
        <td>PATIENTS IDENTIFIER</td>
    </tr>
    <c:forEach var="rownotSavedRecords" items="${failedRows}">
        <tr>
            <td><c:out value="${rownotSavedRecords.position}"></c:out></td>
            <td><c:out value="${rownotSavedRecords.specimenCode}"></c:out></td>
            <td><c:out value="${rownotSavedRecords.HPVResults}"></c:out></td>
            <td><c:out value="${rownotSavedRecords.positiveType}"></c:out></td>
            <td><c:out value="${rownotSavedRecords.patientIdentifier}"></c:out></td>
        </tr>
    </c:forEach>

</table>

<script>
        $(document).ready(function () {
            $('#dwnldBtn').on('click', function () {
                $("#unSavedDateTable").table2excel({
                    filename: "unsaved HPV laboratory results.xls"
                });
            });
        });
    </script>

<%@ include file="/WEB-INF/template/footer.jsp"%>
