package cc.cxsj.nju.reversi.chess;

/**
 * a square of the chess board
 * 
 * @author coldcode
 *
 */
public class Square {
	// piece or no piece in this square
	public boolean empty = true;
	// 0 id black, 1 is white, -1 is empty
	public int color = -1;
	
	public Square() {}
	
	public Square(int color) {
		this.color = color;
	}
	
	public void reset() {
		this.empty = true;
		this.color = -1;
	}
	
	public String toStringToRecord() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder("");
		if (this.empty) {
			sb.append("E");
		} else {
			sb.append("N");
		}
		switch (this.color) {
			case 0:
				sb.append("B");
				break;
			case 1:
				sb.append("W");
				break;
			default:
				sb.append(" ");
				break;
		}
		return sb.toString();
	}
	
	public String toStringToDisplay() {
		// TODO Auto-generated method stub
		if (this.empty) {
			return "|_|";
		} else {
			StringBuilder sb = new StringBuilder("|");
			switch (this.color) {
				case 0:
					sb.append("B");
					break;
				case 1:
					sb.append("W");
					break;
				default:
					return "| |";
			}
			sb.append("|");
			return sb.toString();
		}
	}
}
