<%@include file="header.jsp" %>

<body scroll="no" onLoad="${param.onPageLoad}">
<div class="webpage">

<%@include file="brandHeading.jsp" %>

<%@include file="menu.jsp" %>


    <section>
        <div class="contentWrapper" style="${param.contentWrapperHeight};">  <!--  background-image:url('/~/media/Images/Hero/hero.ashx') -->
            <div class="contentWindow">


            <jsp:include page="${param.modelForm}" flush="true" />


            </div>
        </div>
    </section>


    <%@include file="footer.jsp" %>

</div>

</body>
</html>