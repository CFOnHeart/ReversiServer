package cc.cxsj.nju.reversi.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cc.cxsj.nju.reversi.chess.Square;
import cc.cxsj.nju.reversi.config.ServerProperties;
import cc.cxsj.nju.reversi.info.ContestResult;
import cc.cxsj.nju.reversi.info.ContestResults;
import cc.cxsj.nju.reversi.info.Players;
import cc.cxsj.nju.reversi.info.RecordResolver;
import cc.cxsj.nju.reversi.info.ReplayStep;
import cc.cxsj.nju.reversi.task.CreateServiceRunnable;
import cc.cxsj.nju.reversi.task.DebugServiceRunnable;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	
	private static final int ROWS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.rows"));
	private static final int COLS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.cols"));
	private static final int ROUNDS = Integer.valueOf(ServerProperties.instance().getProperty("contest.rounds"));
	private static final Integer MODE = Integer.valueOf(ServerProperties.instance().getProperty("server.mode"));
    private static final int REPLAY_SPEED = Integer.valueOf(ServerProperties.instance().getProperty("replay.speed"));
	private static final Color THEME_COLOR = Color.CYAN;
	private Thread rt;
	private static MainFrame instance = new MainFrame();
	
	// the selected info
	private ArrayList<Integer> resultsIdList = new ArrayList<Integer>();
	private int selectedContestId = -1;
	private int selectedRound = 0;
	private int stepNum = 0;
	private int mode = 0;

	// ui element
	// west panel
	private DefaultListModel<String> resultListModel;
	private JList<String> resultList;
	private JPopupMenu popupMenu;
	private JMenuItem replayItem = new JMenuItem("Replay Contest");
	private JMenuItem refreshItem = new JMenuItem("Refresh Results");
	private JMenuItem saveResultsItem = new JMenuItem("Save Results");
	private JMenuItem generateNextPlayersItem = new JMenuItem("Generate Promotion Players");
	private JMenuItem nextContestItem = new JMenuItem("Start Next Contest");
	private JMenuItem loadContestItem = new JMenuItem("Load Contest Results");
	private JMenuItem loadTestItem = new JMenuItem("Load Test Results");
	private JMenuItem loadDebugItem = new JMenuItem("Load Debug Results");
	private JMenuItem debugItem = new JMenuItem("Debug");
	private JMenuItem aboutItem = new JMenuItem("About");

	// center panel - grid panel 0 row - north panel
	private JTextField player1info, player2info;
	private JLabel vsLabel = new JLabel("VS");
	private JLabel selectRoundLabel = new JLabel("Round");
	private JComboBox<Integer> roundSelectComboBox;
	private JLabel scoreOfRoundLabel = new JLabel("Score"), scoreVS = new JLabel(":");
	private JTextField score1OfRound, score2OfRound;
	private JLabel errorOfRoundLabel = new JLabel("Error"), errorVS = new JLabel(":");
	private JTextField error1OfRound, error2OfRound;
	private JLabel invalidOfRoundLabel = new JLabel("Invalid Steps"), invlidVS = new JLabel(":");
	private JTextField invalid1OfRound, invalid2OfRound;
	private JLabel stepLabel = new JLabel("Step Info");
	private JTextField stepInfo;

	// center panel - grid panel 0 row - center panel - center panel
	private JLabel[][] chessBoard = new JLabel[ROWS][COLS];

	// center panel - grid panel 0 row - center panel - east panel
	private JLabel stepNumLabel = new JLabel("Num");
	private JTextField stepNumIuput;
	private JButton stepJumpButton = new JButton("Jump");
	private JButton stepBackButton = new JButton("Back");
	private JButton stepNextButton = new JButton("Next");
    private JButton stepAutoReplayButton = new JButton("Auto Replay");
    private JButton stepCtrlAutoReplay = new JButton("Pause");

	// center panel - east panel
	private JTextArea logInfo;
    boolean suspend = false;

	private JTextArea resultInfo;
	JScrollPane scrollResultList;

	private MainFrame() {

		mode = Integer.valueOf(ServerProperties.instance().getProperty("server.mode"));

		// set mainframe layout
		this.setLayout(new BorderLayout());

		// west panel
		JPanel westPanel = new JPanel();
		// westPanel.setSize(100, 400);
		westPanel.setLayout(new BorderLayout());
		// logPanel.setSize(150, 400);
		westPanel.setLayout(new BorderLayout());
		{
			resultListModel = new DefaultListModel<String>();
			// initialize result list
			StringBuilder resultListWidthPlaceHolder = new StringBuilder("");
			int width = Integer.valueOf(ServerProperties.instance().getProperty("ui.result.list.width"));
			for (int w = 0; w < width; w++) {
				resultListWidthPlaceHolder.append("-");
			}
			resultListModel.addElement(resultListWidthPlaceHolder.toString());
			resultList = new JList<String>(resultListModel);
			resultList.setBackground(Color.WHITE);
			scrollResultList = new JScrollPane(resultList);
			scrollResultList.setBackground(Color.WHITE);
			scrollResultList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollResultList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollResultList.setBorder(new TitledBorder("Contest Result"));
			westPanel.add(BorderLayout.CENTER, scrollResultList);
		}
		this.add(BorderLayout.WEST, westPanel);

		// center panel
		JPanel centerPanel = new JPanel();
		// centerPanel.setSize(200, 400);
		centerPanel.setLayout(new BorderLayout(5, 5));
		{
			// center panel - grid panel 0 row
			JPanel centerPanel0Row = new JPanel();
			centerPanel0Row.setLayout(new BorderLayout(5, 5));

			// center panel - grid panel 0 row - north panel
			{
				JPanel centerPanel0RowNorthPanel = new JPanel();
				centerPanel0RowNorthPanel.setLayout(new GridLayout(4, 1));
				centerPanel0RowNorthPanel.setBackground(Color.WHITE);
				centerPanel0RowNorthPanel.setBorder(new TitledBorder("Result Info"));
				{
					JPanel centerPanel0RowNorthPanel0Row = new JPanel();
					centerPanel0RowNorthPanel0Row.setLayout(new FlowLayout(FlowLayout.LEFT));
					centerPanel0RowNorthPanel0Row.setBackground(THEME_COLOR);
					centerPanel0RowNorthPanel0Row.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
					player1info = new JTextField("", 7);
					player1info.setHorizontalAlignment(JTextField.CENTER);
					centerPanel0RowNorthPanel0Row.add(player1info);
					centerPanel0RowNorthPanel0Row.add(vsLabel);
					player2info = new JTextField("", 7);
					player2info.setHorizontalAlignment(JTextField.CENTER);
					centerPanel0RowNorthPanel0Row.add(player2info);
					centerPanel0RowNorthPanel0Row.add(selectRoundLabel);
					roundSelectComboBox = new JComboBox<Integer>();
					roundSelectComboBox.setBackground(Color.WHITE);
					for (int round = 0; round < ROUNDS; round++) {
						roundSelectComboBox.addItem(round);
					}
					selectedRound = 0;
					centerPanel0RowNorthPanel0Row.add(roundSelectComboBox);
					centerPanel0RowNorthPanel.add(centerPanel0RowNorthPanel0Row);
				}
				// score and error
				{
					JPanel centerPanel0RowNorthPanel1Row = new JPanel();
					centerPanel0RowNorthPanel1Row.setLayout(new FlowLayout(FlowLayout.LEFT));
					centerPanel0RowNorthPanel1Row.setBackground(THEME_COLOR);
					centerPanel0RowNorthPanel1Row.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
					centerPanel0RowNorthPanel1Row.add(scoreOfRoundLabel);
					score1OfRound = new JTextField("", 3);
					score1OfRound.setHorizontalAlignment(JTextField.CENTER);
					centerPanel0RowNorthPanel1Row.add(score1OfRound);
					centerPanel0RowNorthPanel1Row.add(scoreVS);
					score2OfRound = new JTextField("", 3);
					score2OfRound.setHorizontalAlignment(JTextField.CENTER);
					centerPanel0RowNorthPanel1Row.add(score2OfRound);
					centerPanel0RowNorthPanel1Row.add(errorOfRoundLabel);
					error1OfRound = new JTextField("", 3);
					error1OfRound.setHorizontalAlignment(JTextField.CENTER);
					centerPanel0RowNorthPanel1Row.add(error1OfRound);
					centerPanel0RowNorthPanel1Row.add(errorVS);
					error2OfRound = new JTextField("", 3);
					error2OfRound.setHorizontalAlignment(JTextField.CENTER);
					centerPanel0RowNorthPanel1Row.add(error2OfRound);
					centerPanel0RowNorthPanel.add(centerPanel0RowNorthPanel1Row);

					centerPanel0RowNorthPanel1Row.add(invalidOfRoundLabel);
					invalid1OfRound = new JTextField("", 3);
					centerPanel0RowNorthPanel1Row.add(invalid1OfRound);
					centerPanel0RowNorthPanel1Row.add(invlidVS);
					invalid2OfRound = new JTextField("", 3);
					centerPanel0RowNorthPanel1Row.add(invalid2OfRound);
				}
				// Num Jump Back Next Auto Ctrl
				{
					JPanel centerPanel0RowNorthPanel2Row = new JPanel();
					centerPanel0RowNorthPanel2Row.setLayout(new FlowLayout(FlowLayout.LEFT));
					centerPanel0RowNorthPanel2Row.setBackground(THEME_COLOR);
					centerPanel0RowNorthPanel2Row.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
					centerPanel0RowNorthPanel2Row.add(stepNumLabel);
					stepNumIuput = new JTextField("0", 3);
					centerPanel0RowNorthPanel2Row.add(stepNumIuput);
					centerPanel0RowNorthPanel2Row.add(stepJumpButton);
					centerPanel0RowNorthPanel2Row.add(stepBackButton);
					centerPanel0RowNorthPanel2Row.add(stepNextButton);
                    centerPanel0RowNorthPanel2Row.add(stepAutoReplayButton);
                    centerPanel0RowNorthPanel2Row.add(stepCtrlAutoReplay);
					centerPanel0RowNorthPanel.add(centerPanel0RowNorthPanel2Row);
				}
				// Step Info
				{
					JPanel centerPanel0RowNorthPanel3Row = new JPanel();
					centerPanel0RowNorthPanel3Row.setLayout(new FlowLayout(FlowLayout.LEFT));
					centerPanel0RowNorthPanel3Row.setBackground(THEME_COLOR);
					centerPanel0RowNorthPanel3Row.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
					centerPanel0RowNorthPanel3Row.add(stepLabel);
					stepInfo = new JTextField("", 27);
					centerPanel0RowNorthPanel3Row.add(stepInfo);
					centerPanel0RowNorthPanel.add(centerPanel0RowNorthPanel3Row);
				}
				centerPanel0Row.add(BorderLayout.NORTH, centerPanel0RowNorthPanel);
			}
			// center panel - grid panel 0 row - center panel
			{
				JPanel centerPanel0RowCenterPanel = new JPanel();
				centerPanel0RowCenterPanel.setLayout(new GridLayout(ROWS, COLS));
				centerPanel0RowCenterPanel.setBorder(new TitledBorder("Chess Board"));
                Color color = new Color(207, 141, 47);
				centerPanel0RowCenterPanel.setBackground(color);
				{
					for (int r = 0; r < ROWS; r++) {
						for (int c = 0; c < COLS; c++) {
							// chessBoard[r][c] = new JTextField(" ");
                            chessBoard[r][c] = new JLabel("");
                            chessBoard[r][c].setBorder(BorderFactory.createLineBorder(color.darker()));
                            chessBoard[r][c].setBackground(color);
                            chessBoard[r][c].setIcon(null);
                            if ((r == 7 && c == 7) || (r == 2 && c == 2) || (r == 12 && c == 2) || (r == 2 && c == 12) || (r == 12 && c == 12))
                                chessBoard[r][c].setBackground(Color.ORANGE);
                            else
                                chessBoard[r][c].setBackground(color);
							chessBoard[r][c].setHorizontalAlignment(JLabel.CENTER);
							centerPanel0RowCenterPanel.add(chessBoard[r][c]);
						}
					}
				}
				centerPanel0Row.add(BorderLayout.CENTER, centerPanel0RowCenterPanel);
			}
			centerPanel.add(centerPanel0Row);
		}
		this.add(BorderLayout.CENTER, centerPanel);

        // center panel - grid panel 1 row
        JPanel logPanel = new JPanel();
        // logPanel.setSize(150, 400);
        logPanel.setLayout(new BorderLayout());
        {
            logInfo = new JTextArea();
            Font font = new Font("Time News Roman", Font.PLAIN, 14);
            logInfo.setFont(font);
            JScrollPane scrollLogInfo = new JScrollPane(logInfo);
            scrollLogInfo.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollLogInfo.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollLogInfo.setBackground(Color.WHITE);
            scrollLogInfo.setBorder(new TitledBorder("Log Info"));
            logPanel.add(BorderLayout.CENTER, scrollLogInfo);
        }
        this.add(BorderLayout.EAST, logPanel);


		// right click menu and response
		popupMenu = new JPopupMenu();
		popupMenu.add(replayItem);
		popupMenu.addSeparator();
		popupMenu.add(refreshItem);
		popupMenu.add(saveResultsItem);
		popupMenu.add(generateNextPlayersItem);
		popupMenu.add(nextContestItem);
		popupMenu.addSeparator();
		popupMenu.add(loadContestItem);
		popupMenu.add(loadTestItem);
		popupMenu.add(loadDebugItem);
		popupMenu.addSeparator();
		popupMenu.add(debugItem);
		popupMenu.addSeparator();
		popupMenu.add(aboutItem);
		switch (MODE) {
            case 0:
                loadTestItem.setEnabled(false);
                loadDebugItem.setEnabled(false);
                debugItem.setEnabled(false);
                break;
            case 1:
                loadContestItem.setEnabled(false);
                loadDebugItem.setEnabled(false);
                generateNextPlayersItem.setEnabled(false);
                nextContestItem.setEnabled(false);
                debugItem.setEnabled(false);
                break;
            case 2:
                loadContestItem.setEnabled(false);
                loadTestItem.setEnabled(false);
                generateNextPlayersItem.setEnabled(false);
                nextContestItem.setEnabled(false);
                break;
            default:
                break;
		}

		replayItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ContestResult result = ContestResults.getContestResult(selectedContestId);
				if (result == null) {
					MainFrame.instance().log("No result with id: " + selectedContestId);
					return;
				}
                // System.out.println("Click replayItem");
				roundSelectComboBox.setSelectedIndex(0);
				selectedRound = 0;

				if (RecordResolver.resolve(mode, result, selectedRound)) {
					player1info.setText("");
					player1info.setText(result.players[0].getId());
					player2info.setText("");
					player2info.setText(result.players[1].getId());
					score1OfRound.setText("");
					score1OfRound.setText(String.valueOf(result.scores[0][selectedRound]));
					score2OfRound.setText("");
					score2OfRound.setText(String.valueOf(result.scores[1][selectedRound]));
					error1OfRound.setText("");
					error1OfRound.setText(String.valueOf(result.errors[0][selectedRound]));
					error2OfRound.setText("");
					error2OfRound.setText(String.valueOf(result.errors[1][selectedRound]));
					stepNum = 0;
					stepNumIuput.setText("0");
					fillChessBoard(0);
				}
			}
		});
		refreshItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshResultListModal();
			}
		});
		saveResultsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ContestResults.saveContestResults();
			}
		});
		generateNextPlayersItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Players.GenerateNextPlayersNameListFile();
			}
		});
		nextContestItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(null, "Are you sure to start next contest?\n"
						+ "Please ensure that the promotion players list has been produced!", "Start next contest?", 
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
				ContestResults.clear();
				refreshResultListModal();
				Players.loadPlayers();
				CreateServiceRunnable.updateService();
			}
		});
		loadContestItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log("Load contest result...");
				mode = 0;
//				ContestResults.loadContestResults(0);
				refreshResultListModal();
			}
		});
		loadTestItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log("Load test result...");
				mode = 1;
//				ContestResults.loadContestResults(1);
				refreshResultListModal();
			}
		});
		loadDebugItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log("Load debug result...");
				mode = 2;
				ContestResults.loadContestResults(2);
				refreshResultListModal();
			}
		});
		debugItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log("Debug...");
				new Thread(new DebugServiceRunnable()).start();
			}
		});
		aboutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Just Enjoy it :-)", "From Qiu Hu & J.Kai Shen", JOptionPane.INFORMATION_MESSAGE,
						null);
			}
		});
		resultList.addMouseListener(new PopupListener());

		// result list selection response
		resultList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int selection = resultList.getSelectedIndex();
				if (selection < resultsIdList.size() && selection >= 0) {
					selectedContestId = resultsIdList.get(selection);
				}
			}
		});

		// chess replay control
		stepJumpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String stepNumS = stepNumIuput.getText();
				if (stepNumS.matches("[0-9]+")) {
					int temp = Integer.valueOf(stepNumS);
					if (temp >= 0 && temp < RecordResolver.record.size()) {
						stepNum = temp;
						fillChessBoard(stepNum);
						log("Jump step " + stepNum);
					} else {
						log("Invalid input step num!");
					}
				} else {
					log("Invalid input step num!");
				}
			}
		});

		stepBackButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (stepNum - 1 >= 0) {
					stepNum--;
					stepNumIuput.setText(String.valueOf(stepNum));
					fillChessBoard(stepNum);
				} else {
					log("Already is the first step, can not back any more!");
				}
			}
		});

		stepNextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (stepNum + 1 < RecordResolver.record.size()) {
					stepNum++;
					stepNumIuput.setText(String.valueOf(stepNum));
					fillChessBoard(stepNum);
                } else {
					log("Already is the last step, can not next any more!");
				}
			}
		});

        stepAutoReplayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startAutoReplay();
            }
        });

        stepCtrlAutoReplay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String txt = stepCtrlAutoReplay.getText();
                if (txt.equals("Pause")) {
                    pauseAutoReplay();
                }
            }
        });

		// select which round
		roundSelectComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int round = roundSelectComboBox.getSelectedIndex();
                    selectedRound = round;
//					if (round < ROUNDS && round >= 0) {
//						System.out.println("debug roundSelectComboBox: "+ContestResults.contestResults.size()
//                                + " select round : "+round);
//						ContestResult result = ContestResults.getContestResult(round);
//						if (result == null) {
//							MainFrame.instance().log("No result with id: " + round);
//                            return;
//						}

					ContestResult result = ContestResults.getContestResult(0);
					if (RecordResolver.resolve(mode, result, selectedRound)) {
						player1info.setText("");
						player1info.setText(result.players[0].getId());
						player2info.setText("");
						player2info.setText(result.players[1].getId());
						score1OfRound.setText("");
						score1OfRound.setText(String.valueOf(result.scores[0][selectedRound]));
						score2OfRound.setText("");
						score2OfRound.setText(String.valueOf(result.scores[1][selectedRound]));
						error1OfRound.setText("");
						error1OfRound.setText(String.valueOf(result.errors[0][selectedRound]));
						error2OfRound.setText("");
						error2OfRound.setText(String.valueOf(result.errors[1][selectedRound]));
						invalid1OfRound.setText(String.valueOf(result.invalidSteps[0][selectedRound]));
						invalid2OfRound.setText(String.valueOf(result.invalidSteps[1][selectedRound]));
						stepNum = 0;
						stepNumIuput.setText("0");
						fillChessBoard(0);
					}
//					}
				}
			}
		});

	}

	class PopupListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			if (e.isPopupTrigger()) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			if (e.isPopupTrigger()) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

    private void startAutoReplay() {
        suspend = false;
        rt = new Thread (
                new Runnable() {
                    public void run() {
                        try {
                            Robot rbt = new Robot();
                            String stepNumS = stepNumIuput.getText();
                            int temp = Integer.valueOf(stepNumS);
                            while (temp + 1 < RecordResolver.record.size()) {
                                stepNextButton.doClick();
                                rbt.delay(REPLAY_SPEED);
                                synchronized (this) {
                                    if (suspend) {
                                        break;
                                    }
                                }
                                temp++;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        rt.start();
    }

    private void pauseAutoReplay() {
        try {
            suspend = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 在ReplayStep中保存好一步的棋盘情况，此函数是用来更新棋盘的
	private void fillChessBoard(int stepNum) {
		ReplayStep step = RecordResolver.record.get(stepNum);
		stepInfo.setText(step.step);
        if (step.isValidStep) {
			for (int r = 0; r < ROWS; r++) {
				for (int c = 0; c < COLS; c++) {
                    chessBoard[r][c].setIcon(step.board[r][c]);
				}
			}
		}
	}

	public void updateChessBoardUI(int lastRow, int lastCol, int r, int c, int color) {
        Color backcolor = new Color(207, 141, 47);

	    if (lastRow != -1) {
            chessBoard[r][c].setIcon(null);
            chessBoard[lastRow][lastCol].setBorder(BorderFactory.createLineBorder(backcolor.darker()));
            chessBoard[lastRow][lastCol].setBackground(backcolor);
        }
        if (color == -1) {
            chessBoard[r][c].setIcon(null);
            chessBoard[lastRow][lastCol].setBorder(BorderFactory.createLineBorder(backcolor.darker()));
            chessBoard[lastRow][lastCol].setBackground(backcolor);
        }
		else
            chessBoard[r][c].setIcon(color == 0 ? ReplayStep.icon_b : ReplayStep.icon_w);
        chessBoard[r][c].setBorder(BorderFactory.createLineBorder(Color.RED, 2));
		// System.out.println(String.format("Put %d %d with color %d", r, c, color));
    }

	public void updateChessboardOneSquare(int r, int c, int color) {
		Color backcolor = new Color(207, 141, 47);

		if (color == -1 || color == 2) {
			chessBoard[r][c].setIcon(null);
		}
		else
			chessBoard[r][c].setIcon(color == 0 ? ReplayStep.icon_b : ReplayStep.icon_w);
		chessBoard[r][c].setBorder(BorderFactory.createLineBorder(backcolor.darker()));
		// System.out.println(String.format("Put %d %d with color %d", r, c, color));
	}

    public void updateStepInfo(String step, int num) {
	    stepInfo.setText(step);
	    stepNumIuput.setText(String.valueOf(num));
    }

    public void setPlayerId(String playerA, String playerB) {
	    player1info.setText(playerA);
	    player2info.setText(playerB);
    }

    public void ClearChessBoardUI() {
        Color color = new Color(207, 141, 47);
	    for (int r = 0; r < ROWS; r++) {
	        for (int c = 0; c < COLS; c++) {
				if( (r == 3 && c == 3) || (r == 4 && c == 4)){
					chessBoard[r][c].setIcon(ReplayStep.icon_b);
				}
				else if( (r == 3 && c == 4) || (r == 4 && c == 3) ){
					chessBoard[r][c].setIcon(ReplayStep.icon_w);
				}
				else{
					chessBoard[r][c].setIcon(null);
				}
                chessBoard[r][c].setText("");
                chessBoard[r][c].setBorder(BorderFactory.createLineBorder(color.darker()));
                chessBoard[r][c].setBackground(color);

	        }
        }
    }

	private void refreshResultListModal() {
		resultsIdList = ContestResults.getContestIdsOrderly();
		int resultNum = resultsIdList.size();
		System.out.println("ResultsIdList has " + resultNum);
		if (resultNum == 0) {
			resultListModel.clear();
			return;
		}
		resultsIdList.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				// TODO Auto-generated method stub
				if (o1 < o2) {
					return -1;
				} else if (o1 > o2) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		resultListModel.clear();
		for (Integer id : resultsIdList) {
		    System.out.println(id);
			ContestResult result = ContestResults.getContestResult(id);
			System.out.println(result);
			StringBuilder sb = new StringBuilder(" ");
			if (id < 10) {
				sb.append("0");
			}
			sb.append(id);
			sb.append("   ");
			sb.append(String.format("%9s", result.players[0].getId()));
			sb.append(" VS ");
			sb.append(String.format("%9s", result.players[1].getId()));
			sb.append("   ");
			sb.append(result.winRound[0]);
			sb.append(":");
			sb.append(result.winRound[1]);
			sb.append("   ");
			resultListModel.addElement(sb.toString());
		}
		log("Refresh result list complete!");
	}

    /**
     * write info on MainFrame's Log Info panel.
     * Cancel `synchronized` key word, let every thread access it freely.
     *
     * @param info contents to write on Log Info panel
     */
	public void log(String info) {    // logInfo is shared variable
        try {
            logInfo.append(info + '\n');
//            System.out.println(info);
            // logInfo.selectAll();
            logInfo.setCaretPosition(logInfo.getText().length());
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public void updateContestResultUI(ContestResult result){
		resultListModel.addElement(result.toString() + "\n");
		resultList = new JList<String>(resultListModel);
		resultList.setBackground(Color.WHITE);
		scrollResultList = new JScrollPane(resultList);
		scrollResultList.setBackground(Color.WHITE);
//		scrollResultList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		scrollResultList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollResultList.setBorder(new TitledBorder("Contest Result"));
	}

    /**
     * invoke static instance of MainFrame to change contents of main panel
     * without `synchronized` key word
     *
     * @return static instance of MainFrame
     */
	public static MainFrame instance() {
		return instance;
	}


}
