<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="HPV Results Entry" otherwise="/index.htm" redirect="/module/cancerscreeninglabresults/cancerscreeninglabresults.htm"/>

    <script src="https://code.jquery.com/jquery-3.6.4.min.js">
    </script>
    <script src="//cdn.rawgit.com/rainabba/jquery-table2excel/1.1.0/dist/jquery.table2excel.min.js">
    </script>

<style>
    .boxHeader{
        background-color: #1aac9b;
        color: White;
        display: block;
        padding : 2px;
        text-align: center;
    }
    .box{
        border: 1px solid   #1aac9b;
    }
    .table {
      width: 100%;
    }
    .formTable {
        text-align: center;
        display: inline-block;
    }
</style>
<br/>

<br/>
<div>
    <b class="boxHeader"> <spring:message code="cancerscreeninglabresults.title" /></b>
    <div class="box">

        <form method="POST"enctype="multipart/form-data" modelAttribute="UploadedResults">
            <body>
                <table>
                  <tr>
                    <td>Encounter Date</td>
                    <td>
                        <input type="date" id="encounterDate" name="encounterDate" required="required">
                    </td>
                  </tr>
                  <tr>
                    <td>Location</td>
                    <td>
                        <select name="selectedLocation" id="selectedLocation" required="required">
                            <option value=""> </option>
                            <c:forEach var="location" items="${locations}">
                                <option value="${location.locationId}" >${location.name}</option>
                            </c:forEach>
                        </select>
                    </td>
                  </tr>
                  <tr>
                    <td>Encounter Type</td>
                    <td>
                        <select name="selectedEncounterType" id="selectedEncounterType" required="required">
                            <c:forEach var="encounterType" items="${encounterTypes}">
                                <c:if test="${encounterType.name== 'Laboratory Encounter'}">
                                    <option value="${encounterType.encounterTypeId}" >${encounterType.name}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </td>
                  </tr>
                  <tr>
                    <td>Provider</td>
                    <td>
                        <select name="selectedProvider" id="selectedProvider" required="required">
                            <option value=""> </option>
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
                        <input id="excelFile" type = "file" name = "excelFile" accept=".csv,.xls,.xlsx" size = "50" required="required"/>
                    </td>
                  </tr>
                  <tr>
                    <td><input id="uploadFile" type = "submit" value = "Upload" /></td>
                  </tr>
                </table>
            </body>
        </form>
    </div>
</div>


<br/> <br/> <br/>
        <button id="dwnldBtn" >
                Export to Excel Sheet
        </button>
 <br/>
<div>
    <b class="boxHeader"> LIST OF HPV RESULTS NOT SAVED </b>
    <div class="box">
        <table id="unSavedDateTable" border='3' class="table">
                <tr>
                    <td><strong>POSITION</strong></td>
                    <td><strong>SPECIMEN CODE</strong></td>
                    <td><strong>HPV RESULTS</strong></td>
                    <td><strong>POSITIVE TYPES</strong></td>
                    <td><strong>PATIENTS IDENTIFIER</strong></td>
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
    </div>
</div>
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
