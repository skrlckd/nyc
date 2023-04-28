
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="model1.BoardTO"%>
<%@page import="model1.BoardDAO"%>
<%@ page import="model1.BoardListTO" %>
<%@page import="java.util.ArrayList"%>
<%
int cpage = 1;
if ( request.getParameter( "cpage" ) != null && !request.getParameter( "cpage" ).equals("") ) {
	cpage = Integer.parseInt( request.getParameter( "cpage" ) );
}

BoardListTO listTO = new BoardListTO();
listTO.setCpage(cpage);

BoardDAO dao = new BoardDAO();
listTO = dao.boardList(listTO);

int recordPerPage = listTO.getRecordPerPage();
int totalRecord = listTO.getTotalRecord();
int totalPage = listTO.getTotalPage();
int blockPerPage = listTO.getBlockPerPage();
int blockRecord = listTO.getBlockRecord();
int startBlock = listTO.getStartBlock();
int endBlock = listTO.getEndBlock();


StringBuffer sbHtml = new StringBuffer();
	
for ( BoardTO to : listTO.getBoardLists() ) {
		blockRecord++;
		sbHtml.append( " <td> ");
		sbHtml.append( " <div>" );
		sbHtml.append( " <table> " );
		sbHtml.append( "<tr> " );
		sbHtml.append( " <td> " );
		sbHtml.append( "<div class='card' style='width:236px;'>");
		sbHtml.append( "<a href='board_view.jsp?cpage="+ cpage +"&seq="+to.getSeq()+"'><img src='upload/" + to.getFilename() + "' class='card-img-top'/></a> " );
		sbHtml.append( "<div class='card-body'>");
		sbHtml.append( "<div class='card-title'>");
		sbHtml.append( "<span class='badge bg-danger me-2'>new</span><strong>" + to.getSubject() + "</strong> " );
		if( to.getWgap() == 0 ) {
			sbHtml.append( "~~");
		}
		sbHtml.append( "</div>");//card-title end
		sbHtml.append( "<div class='card-text'>" );
		sbHtml.append( "<span class='mt-3 mb-3'>" + to.getWriter() + "</span>" );
		sbHtml.append( "<br>" );
		sbHtml.append( "<div class='d-flex justify-content-end fs10 mt-3 mb-3'>" + to.getWdate() + "&nbsp;|&nbsp;Hit " + to.getHit()+ "</div>" );
		sbHtml.append( "</div>" );//card-text end
		
		sbHtml.append( "<div class='d-flex justify-content-end'>" );//오른쪽
		
		sbHtml.append( "<a href='board_view.jsp?cpage="+ cpage +"&seq="+to.getSeq()+"' class='btn btn-primary'>" );
		sbHtml.append( "more" );
		sbHtml.append( "</a>" );
		sbHtml.append( "</div>" );
		sbHtml.append( "</div>" );//card-body end
		
		sbHtml.append( "</div>" );//오른쪽 엔드
		
		sbHtml.append( "</td> " );
		sbHtml.append( "</tr> " );
		sbHtml.append( "</table> " );
		sbHtml.append( " </div> " );
		sbHtml.append( " </td> " );
		
	}
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.3/jquery.min.js"></script>
  <link rel="stylesheet" href="css/custom.css"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>BoardLIST</title>
</head>
<body>
<div class="container">
	<div class="row">
		<div class="col-md-12">
				
				<div class="d-flex mt-3 mb-3">
					<p class="mx-3">
						<img src="img/home.svg" alt="홈아이콘"/>						
					</p>
					<p>
						총
						<span class="txt_orange">
						<%=blockRecord%>
						</span>
						건
					</p>
				</div>
				
				<div class="content_sub">
					
					<!-- 게시판 -->
					<table class="table board_list">
						<tr>
						<%=sbHtml %>
						</tr>
					
					</table>
					
					<div class="d-flex justify-content-between mt-3 align-items-center">
					
					<!-- 페이지넘버 -->
					<ul class="pagination">

							<%
							if(startBlock == 1){
								out.println("<li class='off page-item'><a class='page-link'>&lt;&lt;</a></li>");
							}else{
								out.println("<li class='off page-item'><a class='page-link' href='board_list1.jsp?cpage="+(startBlock-blockPerPage)+">&lt;&lt;</a></li>");	
							}
							/*out.println("&nbsp;");*/
							
							if(cpage == 1){
								out.println("<li class='off page-item' ><a class='page-link'>&lt;</a></li>");
							}else{
								out.println(" <li class='off page-item'><a class='page-link' href='board_list.jsp?cpage="+(cpage-1)+"'>&lt;</a></li> ");
							}
							/*out.println("&nbsp;&nbsp;");*/
							
							for (int i=startBlock; i<=endBlock; i++){
								if(cpage == i) {
									out.println("<li class='off page-item'><a class='page-link'>[" + i + "]</a></li>");
								}else{
									out.println(" <li class='off page-item'><a  class='page-link' href='board_list.jsp?cpage="+i+"'>"+i+"</a></li> ");
								}
							}
							/*out.println("&nbsp;&nbsp;");*/
							
							if (cpage == totalPage) {
								out.println("<li class='off page-item'><a class='page-link'>&gt;</a></li>");
							}else{
								out.println("<li class='off page-item'><a class='page-link' href='board_list.jsp?cpage="+(cpage+1)+"'>&gt;</a></li>");	
							}
							/*out.println("&nbsp;");*/
								
							if(endBlock == totalPage){
								out.println("<li class='off page-item'><a class='page-link'>&gt;</a></li>");
							}else{
								out.println("<li class='off page-item'><a class='page-link' href='board_list.jsp?cpage="+(startBlock+blockPerPage)+"'>&gt;&gt;</a></li>");	
							}
							/*out.println("&nbsp;");*/
							%>												

					</ul>
					
					
					
					
							<input type="button" value="쓰기" class="btn btn-primary" onclick="location.href='board_write.jsp?cpage=<%=cpage%>'"/>
					</div>
					
				</div>
					
			</div>
		</div>
</div>

</body>
</html>