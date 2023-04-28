package model1;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


public class BoardDAO {
	private DataSource dataSource;
	
	public BoardDAO() {
		try {
			Context initCtxContext = new InitialContext();
			Context envCtxContext = (Context)initCtxContext.lookup("java:comp/env");
			this.dataSource = (DataSource)envCtxContext.lookup("jdbc/mysql");
		}catch(NamingException e) {
			System.out.println("[error] : " + e.getMessage());
		}
	}
	
	//write
	public void boardWrite() {
		
	}
	
	//write_ok
	public int boardWriteOk( BoardTO to) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		//정상처리 또는 비정상처리변수 0이면 설정되지 않음을 의미하고 1이면 설정됨을 의미
		//산술계산을 수행했는데 결과가 0이면 flag = 1 다른숫자는 flag=0이 됩니다
		int flag = 1;
		try {
			conn = dataSource.getConnection();
			//데이터베이스에 데이터 집어넣기
			String sql = "INSERT INTO al_board1 VALUES (0, ?, ?, ?, ?, ?, ?, ?,  0, ?, now())";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, to.getSubject());
			pstmt.setString(2, to.getWriter());
			pstmt.setString(3, to.getMail());
			pstmt.setString(4, to.getPassword());
			pstmt.setString(5, to.getContent());
			pstmt.setString(6, to.getFilename());
			pstmt.setLong(7, to.getFilesize());
			pstmt.setString(8, to.getWip());
			
			int result = pstmt.executeUpdate();
			if(result == 1) {
				flag = 0;
			}
		}catch (SQLException e) {
			System.out.println("error : " + e.getMessage());
		}finally {
			if(pstmt !=null) try {pstmt.close();} catch (SQLException e) {}
			if(conn !=null) try {conn.close();} catch (SQLException e) {}
		}
		return flag;
	}
	
	//list
	public ArrayList<BoardTO> boardList() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		ArrayList<BoardTO> lists = new ArrayList<BoardTO>();

		
		try {
			conn = dataSource.getConnection();
			
			String sql = "SELECT seq, subject, filename, writer, date_format(wdate, '%Y-%m-%d') wdate, hit, datediff(now(), wdate) wgap FROM al_board1 ORDER BY seq DESC";
			pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs=pstmt.executeQuery();
			/*Resultset 에서 rs.next를 사용하면 다음 결과 row를 가져오고 다음에는 이전 값을 사용못하게 되는데 이옵션을 스게 되면 
			한번 커서가 지나간 다음에 다시 되돌릴수 있습니다
			ResultSet.CONCUR_READ_ONLY 다시 insert나 update 로 사용하지 않는다는 의미입니다
			*/

			
			//데이터베이스에서 글목록을 가져와서 리스트를 나타내기
			while( rs.next() ) {
				BoardTO to = new BoardTO();
				String seq = rs.getString( "seq" );
				String subject = rs.getString( "subject" );
				String writer = rs.getString( "writer" );
				String filename = rs.getString( "filename" );
				String wdate = rs.getString( "wdate" );
				String hit = rs.getString( "hit" );
				int wgap = rs.getInt( "wgap" );
				
				to.setSeq(seq);
				to.setSubject(subject);
				to.setWriter(writer);
				to.setFilename(filename);
				to.setWdate(wdate);
				to.setHit(hit);
				to.setWgap(wgap);
				
				lists.add( to );
			}
			
		} catch (SQLException e) {
			System.out.println( "error : " + e.getMessage() );
		} finally {
			if ( rs != null ) try { rs.close(); } catch ( SQLException e ) {}
			if ( pstmt != null ) try { pstmt.close(); } catch ( SQLException e ) {}
			if ( conn != null ) try { conn.close(); } catch ( SQLException e ) {}
		}
		return lists;
	}
	
	//paging list
	public BoardListTO boardList( BoardListTO listTO ) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		// 페이지를 위한 기본 요소
		int cpage = listTO.getCpage();
		int recordPerPage = listTO.getRecordPerPage();	//한페이지에 보이는 글의 개수  5개
		int BlockPerPage = listTO.getBlockPerPage();	//한 화면에 보일 페이지의 수 3개
		
		try {
			conn = dataSource.getConnection();
			String sql = "SELECT seq, subject, filename, writer, date_format(wdate, '%Y-%m-%d') wdate, hit, datediff(now(), wdate) wgap FROM al_board1 ORDER BY seq DESC";
			pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs=pstmt.executeQuery();
			
			//총글의 개수 얻기
			rs.last();
			listTO.setTotalRecord( rs.getRow() );
			rs.beforeFirst();
			
			//총 페이지의 수 얻기
			listTO.setTotalPage( ( (listTO.getTotalRecord() - 1) / recordPerPage ) + 1 );
			int skip = ( cpage * recordPerPage ) - recordPerPage;
			if (skip != 0) rs.absolute( skip );//rs.next보다 간단한 종류의 데이터베이스[FORWARD_ONLY] 커서를 얻습니다
			//모든 결과를 반복해야되고 특정결과로 건너 뛰려면 사용합니다
			ArrayList<BoardTO> lists = new ArrayList<BoardTO>();
			
			for( int i=0; i<recordPerPage && rs.next(); i++) {
				BoardTO to = new BoardTO();
				String seq = rs.getString( "seq" );
				String subject = rs.getString( "subject" );
				String writer = rs.getString( "writer" );
				String filename = rs.getString( "filename" );
				String wdate = rs.getString( "wdate" );
				String hit = rs.getString( "hit" );
				int wgap = rs.getInt( "wgap" );
				
				to.setSeq(seq);
				to.setSubject(subject);
				to.setWriter(writer);
				to.setFilename(filename);
				to.setWdate(wdate);
				to.setHit(hit);
				to.setWgap(wgap);
				
				lists.add( to );
			}
			listTO.setBoardLists(lists);
			listTO.setStartBlock(((cpage-1)/BlockPerPage) * BlockPerPage + 1);
			listTO.setEndBlock(((cpage-1)/BlockPerPage) * BlockPerPage +  BlockPerPage);
			if(listTO.getEndBlock() >= listTO.getTotalPage()) {
				listTO.setEndBlock(listTO.getTotalPage());
			}
		}catch(SQLException e) {
			System.out.println("error : " + e.getMessage());
		}finally {
			if(rs !=null) try {rs.close();} catch (SQLException e) {}
			if(pstmt !=null) try {pstmt.close();} catch (SQLException e) {}
			if(conn !=null) try {conn.close();} catch (SQLException e) {}
		}
		return listTO;
	}
	
	//view
	public BoardTO boardView( BoardTO to ) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			conn = dataSource.getConnection();
			
			//조회수 증가시키기
			String sql = "UPDATE al_board1 set hit = hit+1 WHERE seq = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, to.getSeq() );
			rs = pstmt.executeQuery();
			//첫번째 물음표의 값을 지정 인덱스는 1이어서 
			//이후희 물음표의 인덱스는 나오는 순서대로의 인덱스값이 1씩 증가
			/*pstmt.setString(2, to.getSeq());//두번째 물음표의 값을 지정*/
			
			//데이터베이스에서 해당 글 내용 가져오기
			sql = "SELECT subject, writer, mail, content, filename, hit, wip, wdate from al_board1 WHERE seq = ?";
			pstmt = conn.prepareStatement( sql );
			pstmt.setString( 1, to.getSeq() );
			rs = pstmt.executeQuery();
			
			//데이터베이스에서 sql실행문의 각 컬럼을 가져와서 변수에 저장
			if ( rs.next() ) {
				String subject = rs.getString( "subject" );
				String writer = rs.getString( "writer" );
				String mail = rs.getString( "mail" );
				String content = rs.getString( "content" );
				String filename = rs.getString( "filename" );
				String hit = rs.getString( "hit" );
				String wip = rs.getString( "wip" );
				String wdate = rs.getString( "wdate" );
				
				to.setSubject(subject);
				to.setWriter(writer);
				to.setMail(mail);
				to.setContent(content);
				to.setFilename(filename);
				to.setHit(hit);
				to.setWip(wip);
				to.setWdate(wdate);
			}
		} catch (SQLException e) {
			System.out.println( "error : " + e.getMessage() );
		} finally {
			if ( rs != null ) try { rs.close(); } catch ( SQLException e ) {}
			if ( pstmt != null ) try { pstmt.close(); } catch ( SQLException e ) {}
			if ( conn != null ) try { conn.close(); } catch ( SQLException e ) {}
		}
		return to;
	}
	
	//view_이전글
	public BoardTO boardView_before(BoardTO to_before) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			conn = dataSource.getConnection();
			//데이터베이스에서 해당 글 내용 가져오기
			String sql = "SELECT seq, subject FROM al_board1 WHERE seq = (select max(seq) FROM al_board1 WHERE seq < ?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, to_before.getSeq());
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				String subject = rs.getString("subject");
				String seq = rs.getString("seq");
				to_before.setSubject(subject);
				to_before.setSeq(seq);
			}else {
				to_before.setSubject("이전글이 없습니다");
			}
		}catch(SQLException e) {
			System.out.println("error : " + e.getMessage());
		}finally {
			if(rs !=null) try {rs.close();} catch (SQLException e) {}
			if(pstmt !=null) try {pstmt.close();} catch (SQLException e) {}
			if(conn !=null) try {conn.close();} catch (SQLException e) {}
		}
		return to_before;
	}
	
	//view 다음글
	public BoardTO boardView_next(BoardTO to_next) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			conn = dataSource.getConnection();
			//데이터베이스에서 해당 글 내용 가져오기
			String sql = "SELECT seq, subject FROM al_board1 WHERE seq = (SELECT min(seq) FROM al_board1 WHERE seq > ?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, to_next.getSeq());
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				String subject = rs.getString("subject");
                String seq = rs.getString("seq");
                to_next.setSubject(subject);
                to_next.setSeq(seq);
			}else {
				to_next.setSubject("다음글이 없습니다");
			}
		}catch (SQLException e) {
			System.out.println("error : " + e.getMessage());
		}finally {
			if(rs !=null) try {rs.close();} catch (SQLException e) {}
			if(pstmt !=null) try {pstmt.close();} catch (SQLException e) {}
			if(conn !=null) try {conn.close();} catch (SQLException e) {}
		}
		return to_next;
	}

	//delete
	public BoardTO boardDelete(BoardTO to) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs =  null;
		
		try {
			conn =dataSource.getConnection();
			
			//데이터베이스에서 해당 글 내용 가져오기
			String sql = "SELECT subject, writer FROM al_board1 WHERE seq = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, to.getSeq());
			rs = pstmt.executeQuery();
			
			//데이터베이스에서 sql실행문의 각 컬럼을 가져와서 변수에 저장
			if(rs.next()) {
				String subject = rs.getString("subject");
				String writer = rs.getString("writer");
				
				to.setSubject(subject);
				to.setWriter(writer);
			}
		}catch (SQLException e) {
			System.out.println("error : " + e.getMessage());
		}finally {
			if(rs !=null) try {rs.close();} catch (SQLException e) {}
			if(pstmt !=null) try {pstmt.close();} catch (SQLException e) {}
			if(conn !=null) try {conn.close();} catch (SQLException e) {}
		}
		return to;
	}
	
	//delete_ok
	public int boardDeleteOk(BoardTO to) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs =  null;
		
		int flag = 2;
		
		try {
			conn = dataSource.getConnection();
			
			String sql = "SELECT filename FROM al_board1 WHERE seq = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, to.getSeq());
			
			rs = pstmt.executeQuery();
			String filename = null;
			if(rs.next()) {
				filename = rs.getString("filename");
			}
			//데이터베이스에서 해당 글 내용 가져오기
			sql = "DELETE FROM al_board1 WHERE seq = ? and password = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, to.getSeq());
			pstmt.setString(2, to.getPassword());
			
			int result = pstmt.executeUpdate();
			if(result == 0) {
				flag = 1;
			}else if (result == 1) {//첨부파일이 있다면...사진
				flag = 0;
				if (filename != null) {
					File file = new File("D:/dev4/jsp_project/photo/src/main/webapp/upload/" + filename);
					file.delete();
				}
			}
		}catch (SQLException e) {
			System.out.println("error : " + e.getMessage());		
		}finally {
			if(pstmt !=null) try {pstmt.close();} catch (SQLException e) {}
			if(conn !=null) try {conn.close();} catch (SQLException e) {}
		}
		return flag;
	}
	//modify
	public BoardTO boardModify(BoardTO to) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			conn = dataSource.getConnection();
			
			//데이터베이스에서 해당글 내용 가져오기
			String sql = "SELECT writer, subject, content, mail, filename FROM al_board1 WHERE seq = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, to.getSeq());
			rs = pstmt.executeQuery();
			
			//데이터베이스에서 sql실행문의 각 컬럼을 가져와서 변수에 저장
			if(rs.next()) {
				String subject = rs.getString("subject");
				String writer = rs.getString("writer");
				String content = rs.getString("content");
				String filename = rs.getString("filename");
				String mail = rs.getString("mail");
				
				to.setSubject(subject);
				to.setWriter(writer);
				to.setContent(content);
				to.setFilename(filename);
				to.setMail(mail);
			}
		}catch (SQLException e) {
			System.out.println("error : "+e.getMessage());
		}finally {
			if(rs !=null) try {rs.close();} catch (SQLException e) {}
			if(pstmt !=null) try {pstmt.close();} catch (SQLException e) {}
			if(conn !=null) try {conn.close();} catch (SQLException e) {}
		}
		return to;
	}
	//modify_ok
	public int boardModifyOk (BoardTO to) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		int flag = 2;
		try {
			conn = dataSource.getConnection();
			
			String sql = "SELECT filename FROM al_board1 WHERE seq = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, to.getSeq());
			rs = pstmt.executeQuery();	
			String oldFilename = null;
			if(rs.next()) {
				oldFilename = rs.getString("filename");
			}
			//수정에서 첨부파일이 있을때
			if(to.getFilename() != null) {
				sql = "UPDATE al_board1 set subject =?, content=?, mail =?, filename=? WHERE seq=? and password = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, to.getSubject());
				pstmt.setString(2, to.getContent());
				pstmt.setString(3, to.getMail());
				pstmt.setString(4, to.getFilename());
				pstmt.setString(5, to.getSeq());
				pstmt.setString(6, to.getPassword());
			}else {//수정에서 첨부파일이 없을때
				sql = "UPDATE al_board1 set subject =?, content=?, mail =? WHERE seq=? and password = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, to.getSubject());
				pstmt.setString(2, to.getContent());
				pstmt.setString(3, to.getMail());
				pstmt.setString(4, to.getSeq());
				pstmt.setString(5, to.getPassword()); 
		}
		int result = pstmt.executeUpdate();
		if(result == 0) {
			flag =1;
		}else if (result == 1) {
					flag = 0;
					if(to.getFilename() !=null && oldFilename != null) {
					//기존 첨부파일이 있고 추가된 첨부파일이 있을경우 기존파일은 삭제한다
					File file = new File("D:/dev4/jsp_project/photo/src/main/webapp/upload/" + oldFilename);
					file.delete();
				}
			}
	}catch (SQLException e) {
		System.out.println("error : " + e.getMessage());
	}finally {
		if(pstmt !=null) try {pstmt.close();} catch (SQLException e) {}
		if(conn !=null) try {conn.close();} catch (SQLException e) {}
	}
	return flag;
	}
}
	
