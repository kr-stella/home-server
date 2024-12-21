package jj.stella.entity.vo;

/** ellipsis paging */
public class PagingVo {
	
	private int totalCnt;		// 게시물 총 갯수
	private int page;			// 현재 페이지
	private int pageCnt;		// 페이징
	private int perCnt;			// 한 페이지에 보여질 게시물의 개수
	
	private int lastPage;		// 마지막 페이지
	
	/** 게시물 총 갯수 ( 게시물의 개수를 기준으로 페이징 데이터를 설정 ) */
	public int getTotalCnt() {
		return totalCnt;
	};
	public void setTotalCnt(int totalCnt) {
		this.totalCnt = totalCnt;
		setPaging();
	};
	
	/** 현재 페이지 */
	public int getPage() {
		return page;
	};
	public void setPage(int page) {
		this.page = page;
	};
	
	/** 페이징 수량 */
	public int getPageCnt() {
		return pageCnt;
	};
	public void setPageCnt(int pageCnt) {
		this.pageCnt = pageCnt;
	};
	
	/** 한 페이지에 보여질 게시물의 개수 */
	public int getPerCnt() {
		return perCnt;
	};
	public void setPerCnt(int perCnt) {
		this.perCnt = perCnt;
	};
	
	/** 마지막 페이지 */
	public int getLastPage() {
		return lastPage;
	};
	
	/** 마지막 페이지 계산 */
	private void setPaging() {
		lastPage = (int) Math.ceil(this.totalCnt / (double) this.perCnt);
	};
	
}