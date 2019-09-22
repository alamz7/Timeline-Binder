<%-- 
    Document   : index
    Created on : Jan 30, 2019, 10:33:41 PM
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
    Boolean noResult = (Boolean)request.getAttribute("noResult"); 
    Boolean noInput = (Boolean)request.getAttribute("noInput");
%>
<div id="body">
    <div id="site_content" style="margin-top: 150px; margin-left: 400px;">
     <script>
        activePage = "home";
    </script>
    <form action="${pageContext.request.contextPath}/IndexServlet" method="post">
         <label>Enter Keyword:</label>
    <input style="height: 26px; width:300px; margin: 3px;padding-left: 5px" type="input" name="keyword" value="" />
    <input style="height: 30px; width:70px;" type="submit" name="btnSearch" value="Search" />
    
    <% if(noResult!=null){if(noResult){ %>
    <br/><br/>
    <label style="margin-left: 92px;">No result found!</label>
    <%}}%>
    
    <% if(noInput!=null){if(noInput){ %>
    <br/><br/>
    <label style="margin-left: 92px;">Enter keyword, please!</label>
    <%}}%>
    </form>        
 </div>   
</div>
  
<%@include file="WEB-INF/jspf/footer.jspf" %>