package cc.cxsj.nju.reversi.info;

import cc.cxsj.nju.reversi.config.ServerProperties;

import javax.swing.*;
import java.awt.*;

public class ReplayStep {
	
	private static final int ROWS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.rows"));
	private static final int COLS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.cols"));
    static String path_black = System.getProperty("user.dir") + "/Icons/black.png";
    static String path_white = System.getProperty("user.dir") + "/Icons/white.png";
    public static Icon icon_b = getImageIcon(path_black, 22, 22);
    public static Icon icon_w = getImageIcon(path_white, 22, 22);
	private static Icon[] trans2label = new Icon[]{icon_b, icon_w};
	
	public String step = null;
	// public String[][] board = null;
	public Icon[][] board = null;
	public boolean isValidStep = false;
	
	public ReplayStep(String step, boolean isValid) {
		this.step = step;
		this.isValidStep = isValid;
		if (isValid) {
			board = new Icon[ROWS][COLS];
		}
	}

    public static ImageIcon getImageIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        icon.setImage(icon.getImage().getScaledInstance(width, height,
                Image.SCALE_DEFAULT));
        return icon;
    }

	public void append(int r, String line) {
//		String[] row = line.split(" ");
		for (int c = 0; c < COLS; c++) {
//			System.out.printf(line.substring(4*c+1, 4*c+1));
			switch (line.charAt(4*c+1)) {
				case 'B':
                {
					board[r][c] = trans2label[0];
					break;
				}
				case 'W':
				{
					board[r][c] = trans2label[1];
					break;
				}
				default:
					board[r][c] = null;
					break;
            }
		}
//		System.out.println("");
	}
}
