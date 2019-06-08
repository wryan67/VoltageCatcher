<%@ page import="com.acxiom.ccsi.idmgmt.controllers.SettingsController" %>
<%@ page import="com.acxiom.ccsi.idmgmt.core.Util" %>
<section>
        <div class="menu">
            <div class="menuLeft">
                <ul>
                    <li>
                        <a href="${param.baseURL}/">[home]</a>
                    </li>
                    <li>
                        <%=SettingsController.getRelease()%>
                    </li>
                </ul>
            </div>
            <div class="menuRight">
                <ul>
                <%
                   if (request.getUserPrincipal()!=null && !request.getUserPrincipal().equals("")) {  %>
                <li>
                  user:<%=request.getUserPrincipal()%>
                </li>
                <% } %>
                    <li>
                        <%=Util.getInstanceName()%>
                    </li>
                </ul>
            </div>
        </div>
    </section>
