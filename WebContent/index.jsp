<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link
	href="${pageContext.request.contextPath}/bootstrap/css/bootstrap.css"
	rel="stylesheet">
<title>首页</title>
</head>
<body>
<div class="container-fluid" >
	<div class="row-fluid">
		<div class="span12">
			<h3>
				一、部署篇
			</h3>
			<div>
				1、在项目的config/course.properties文件内配置合适阶段的参数。
			</div>
			<div>
				2、在config/applicationContext.xml文件中，修改#1对应的数据库配置信息。
			</div>
			<div>
				3、在浏览器地址栏中输入：http://localhost:8080/pwork
			</div>
			
			<h3>
				二、运行篇
			</h3>
			<p>
				由于猿题库官网的结构因素，运行时有两套逻辑。初中高中公用一套，高考单独使用一套。
			</p>
			<p>
				<strong>初高中：</strong>
			</p>
			<ol>
				<li>
					初始化课程。 点击<a href="${pageContext.request.contextPath}/ytk/course">加载课程信息</a>进行初始化。
				</li>
				<li>
					初始化各版本和册信息。点击<a href="${pageContext.request.contextPath}/ytk/courseVersionGrade">初始化版本和册信息</a>进行初始化。
				</li>
				<li>
					加载课程目录树。点击<a href="${pageContext.request.contextPath}/ytk/courseTree">加载课程目录树</a>进行加载。
				</li>
				<li>
					下载题目。点击<a href="${pageContext.request.contextPath}/ytk/questions">下载题目</a>。
				</li>
			</ol>
			<p>
				<strong>高考：</strong>
			</p>
			<ol>
				<li>
					初始化考试类型和各科信息。点击<a href="${pageContext.request.contextPath}/ytk/pullGKTypeCourse">初始化考试类型和各科信息</a>进行初始化。
				</li>
				<li>
					加载高考课程树。点击<a href="${pageContext.request.contextPath}/ytk/pullGKCourseTree">加载高考课程树</a>。
				</li>
				<li>
					下载题目。点击<a href="${pageContext.request.contextPath}/ytk/pullGKQuestions">下载题目</a>。
				</li>
			</ol>
			<p>
				<strong>图片解析和下载（初中，高中，高考通用）：</strong>
			</p>
			<ol>
				<li>
					解析图片入库。点击<a href="${pageContext.request.contextPath}/ytk/parseImg">解析图片</a>。
				</li>
				<li>
					下载图片。点击<a href="${pageContext.request.contextPath}/ytk/downloadImg">下载图片</a>。
				</li>
			</ol>
			
			
		</div>
	</div>
</div>
<!-- 	<div class="container-fluid"> -->
<!-- 		<h1>It works!</h1> -->

<!-- 		<div class="row-fluid"> -->
<!-- 			<div class="span3"> -->
<!-- 				<a class="btn btn-success" -->
<%-- 					href="${pageContext.request.contextPath}/ytk/course">初始化初高中课程</a> --%>
<!-- 			</div> -->
<!-- 			<div class="span3"> -->
<!-- 				<a class="btn btn-success" -->
<%-- 					href="${pageContext.request.contextPath}/ytk/courseVersionGrade">初始化初高中各科目版本和册信息</a> --%>
<!-- 			</div> -->
<!-- 			<div class="span3"> -->
<!-- 				<a class="btn btn-success" -->
<%-- 					href="${pageContext.request.contextPath}/ytk/courseTree">加载初高中目录树</a> --%>
<!-- 			</div> -->
<!-- 			<div class="span3"> -->
<!-- 				<a class="btn btn-success" -->
<%-- 					href="${pageContext.request.contextPath}/ytk/questions">下载题目JSON文件</a> --%>
<!-- 			</div> -->
<!-- 		</div> -->

<!-- 		<div class="row-fluid"> -->
<!-- 			<div class="span4"> -->
<!-- 				<a class="btn btn-info" -->
<%-- 					href="${pageContext.request.contextPath}/ytk/pullGKTypeCourse">初始化高考考试类型和各科目信息</a> --%>
<!-- 			</div> -->
<!-- 			<div class="span4"> -->
<!-- 				<a class="btn btn-info" -->
<%-- 					href="${pageContext.request.contextPath}/ytk/pullGKCourseTree">加载高考课程树</a> --%>
<!-- 			</div> -->
<!-- 			<div class="span4"> -->
<!-- 				<a class="btn btn-info" -->
<%-- 					href="${pageContext.request.contextPath}/ytk/pullGKQuestions">下载高考题目JSON</a> --%>
<!-- 			</div> -->
<!-- 		</div> -->
<!-- 		<div class=row""> -->
<!-- 			<a class="btn btn-danger" -->
<%-- 				href="${pageContext.request.contextPath}/ytk/parseImg">解析图片入库</a> --%>
<!-- 			<a class="btn btn-danger" -->
<%-- 				href="${pageContext.request.contextPath}/ytk/downloadImg">下载图片</a> --%>
<!-- 		</div> -->
<!-- 	</div> -->


	<script type="text/javascript"
		src="${pageContext.request.contextPath}/js/jquery-1.8.2.js"></script>
	<script type="text/javascript"
		src="${pageContext.request.contextPath}/bootstrap/js/bootstrap.js"></script>
</body>
</html>