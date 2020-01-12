<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>ElasticSearch HighLevelClient - Test</title>

    <!-- 부트스트랩 -->
    <link href="${cp}/bootstrap/css/bootstrap.min.css" rel="stylesheet">
    <!-- 서비스 스타일 -->
    <link href="${cp}/service/css/style.css" rel="stylesheet">
    <!-- rangeSlider -->
    <link href="${cp}/service/css/ion.rangeSlider.css" rel="stylesheet">

    <!-- IE8 에서 HTML5 요소와 미디어 쿼리를 위한 HTML5 shim 와 Respond.js -->
    <!-- WARNING: Respond.js 는 당신이 file:// 을 통해 페이지를 볼 때는 동작하지 않습니다. -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->

    <!-- jQuery (부트스트랩의 자바스크립트 플러그인을 위해 필요합니다) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
    <script src="${cp}/service/js/ion.rangeSlider.min.js"></script>


    <script>
        $(function () {
            $('.input-group.date').datepicker({
                calendarWeeks: false,
                todayHighlight: true,
                autoclose: true,
                format: "yyyy-mm-dd",
                language: "kr"
            });
        });


    </script>

</head>
<body role="document">

<nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="#">ELK Stack</a>
        </div>
        <div class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
                <li class="active"><a href="#">Home</a></li>
                <li><a href="#about">About</a></li>
                <li><a href="#contact">Contact</a></li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Dropdown
                        <span class="caret"></span></a>
                    <ul class="dropdown-menu" role="menu">
                        <li><a href="#">Action</a></li>
                        <li><a href="#">Another action</a></li>
                        <li><a href="#">Something else here</a></li>
                        <li class="divider"></li>
                        <li class="dropdown-header">Nav header</li>
                        <li><a href="#">Separated link</a></li>
                        <li><a href="#">One more separated link</a></li>
                    </ul>
                </li>
            </ul>
        </div><!--/.nav-collapse -->
    </div>
</nav>


<div id="test-content" class="container theme-showcase" role="main">
    <div class="row">
        <div class="col-sm-8">

            <div class="jumbotron">
                <h1>Product Search Test</h1>
                <p>Elasticsearch request & response Test.</p>
                <p>Scraping Date : 2019.12.31 05:03:55 / DataCnt : 100</p>
            </div>

        </div><!-- /.col-sm-8 -->
        <div class="col-sm-4">

            <form class="form-signin" id="frm_hotelSearch" action="/">
                <h2 class="form-signin-heading">PROD Search</h2>

                <label for="prodNm" class="sr-only">상품명</label>
                <input type="text" id="prodNm" class="form-control" name="prodNm" placeholder="상품명"
                       autofocus="" value="${prodNm}">
                <label for="marketName" class="sr-only">스토어명</label>
                <input type="text" id="marketName" class="form-control" name="marketName" placeholder="스토어명"
                       autofocus="" value="${marketName}">
                <input type="text" id="rangePrimary" name="rangePrimary" value="${rangePrimary}" />
                <p id="priceRangeSelected"></P>


                <button class="btn btn-lg btn-primary btn-block" type="submit" style="margin-top:5px;">Search</button>
            </form>


<%--            <div class="panel panel-default">--%>
<%--                <div class="panel-heading">--%>
<%--                    <h3 class="panel-title">Response</h3>--%>
<%--                </div>--%>
<%--                <div class="panel-body">--%>
<%--                    ${resultJson}--%>
<%--                </div>--%>
<%--            </div>--%>

        </div>


    </div>

    <div class="row">
        <c:forEach items="${resultJson.hits.hits}" var="hit" varStatus="st">
        <div class="col-sm-4">
            <a href="${hit._source.prodUrl}" target="_blank">
                <div class="alert alert-info" role="alert" style="min-height: 450px;">
                    <img width="100%" src="${hit._source.prodImg}" style="margin-bottom:10px;">
                    <strong>상품명 : </strong> ${hit._source.prodNm}<br>
                    <strong>가격 : </strong> ${hit._source.price}<br>
                    <strong>스토어명 : </strong> ${hit._source.marketName}<br>
                </div>
            </a>
        </div>
        </c:forEach>
    </div>
</div>


<!-- 모든 컴파일된 플러그인을 포함합니다 (아래), 원하지 않는다면 필요한 각각의 파일을 포함하세요 -->
<script src="${cp}/bootstrap/js/bootstrap.min.js"></script>
<!-- DatePicker Add -->
<link rel="stylesheet"
      href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datepicker/1.5.0/css/bootstrap-datepicker3.min.css">
<script type='text/javascript'
        src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datepicker/1.5.0/js/bootstrap-datepicker.min.js"></script>
<script src="${cp}/bootstrap/js/bootstrap-datepicker.kr.js" charset="UTF-8"></script>


<script>

    var rangePrimary = "${rangePrimary}";
    var rangeFrom;
    var rangeTo;
    viewPrice();

    if(rangePrimary === ""){
        rangeFrom = 0;
        rangeTo = 350000;
    }else{
        var rangeValue = rangePrimary.split(";");
        rangeFrom = rangeValue[0];
        rangeTo = rangeValue[1];
    }

    $("#rangePrimary").ionRangeSlider({
        type: "double",
        grid: true,
        min: 0,
        max: 350000,
        from: rangeFrom,
        to: rangeTo,
        step: 100,
        // prefix: "원"
        postfix: "원"
    });

    $("#rangePrimary").on("change", function () {
        viewPrice();
    });

    function viewPrice() {
        var $this = $("#rangePrimary"),
            value = $this.prop("value").split(";");
        var minPrice = value[0];
        var maxPrice = value[1];
        $("#priceRangeSelected").text("Min = "+minPrice + "원 / Max = " + maxPrice + "원");
    }
</script>
</body>

</html>


