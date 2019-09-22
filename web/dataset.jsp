<%-- 
    Document   : dataset
    Created on : Feb 14, 2019, 5:57:07 PM
    Author     : alamz
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@include file="WEB-INF/jspf/header.jspf" %>
<%  
    List<String> dataSet = new ArrayList<String>();
    List<String> descritptionList = new ArrayList<String>();
    dataSet = (List)request.getAttribute("dataSet");
    descritptionList = (List)request.getAttribute("descriptionList"); 
    
    Boolean noFile = (Boolean)request.getAttribute("noFile"); 
%>
<div id="body">
<div id="site_content">
    
    <form action="${pageContext.request.contextPath}/datasetservlet" method="post">
    <table>
        <tr>
            <th>
                S.No
            </th>
              <th>
                Video URL
            </th>
              <th>
                Description
            </th>
        </tr>
            <%  
                if(dataSet!= null){ for(int i=0; i < dataSet.size(); i++){ %>
              <tr>
             <td>
                 <%= i+1 %>
            </td>
              <td>
                <%= dataSet.get(i)%>
            </td>
              <td>
                <%= descritptionList.get(i)%>
            </td>
              </tr>
            <% }} %>       
    </table>
     <%-- 
     <label>Enter URL:</label>
     <input type="input" name="url" value="" />
     --%>
    <input style="height: 30px; width:70px; margin-left: 640px;" type="submit" name="btnGetSrt" value="Get Srt" />
    <input style="height: 30px; width:70px;" type="submit" name="btnModifySrt" value="Modify Srt" />
    
     <% if(noFile!=null){if(noFile){ %>
    <br/><br/>
    <label style="margin-left: 92px;">No file found!</label>
    <%}}%>
    <%-- 
    <input type="submit" name="btnGetKeywords" value="Get Keywords" />
    --%>
     </form>    
 </div>   
</div>
  
<%@include file="WEB-INF/jspf/footer.jspf" %>




