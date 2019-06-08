<%@ page import="org.wryan67.vc.common.Util" %>
<%

   if (Util.getInstanceName().equals("prod")  && !request.getRequestURI().contains("monitor.jsp")) {
//       System.out.println("war request URI="+request.getRequestURI());
//       System.out.println("war request URL="+request.getRequestURL());
//       System.out.println("war request ServerName="+request.getServerName());
//       System.out.println("war request protocol="+request.getProtocol());
//       System.out.println("war request ContextPath="+request.getContextPath());
//       System.out.println("war request PathInfo()="+request.getPathInfo());

       if (!request.getServerName().toLowerCase().equals(Util.getAppserverId()) && !request.getServerName().toLowerCase().equals(Util.getAppserverId().split("\\.")[0]) ) {
           response.addHeader("Location", "monitor.jsp");
           response.setStatus(HttpServletResponse.SC_FOUND);
           return;
       }
   }
   String baseURL="/idmgmt";

//   if (RACCEntryPoint.isHttps(request)) {
//       baseURL = "https://" + request.getServerName();
//       if (request.getServerPort()!=443 && request.getServerPort()!=80) {
//           baseURL+=":"+request.getServerPort();
//       }
//   }else{
//       baseURL = "http://" + request.getServerName()+":"+request.getServerPort();
//   }
//   baseURL+="/idmgmt";


   response.addHeader("X-Frame-Options","NONE");
   response.addHeader("Cache-Control","private, no-cache, no-store");
   response.addHeader("Strict-Transport-Security", "max-age=31536000");



%>