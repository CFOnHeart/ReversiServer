package cc.cxsj.nju.reversi.info;

import cc.cxsj.nju.reversi.config.ServerProperties;

public class ContestResult {
	
	private static final int ROUNDS = Integer.valueOf(ServerProperties.instance().getProperty("contest.rounds"));
	private static final int ERRORS = Integer.valueOf(ServerProperties.instance().getProperty("step.error.number"));
	
	public int id;
	public Player[] players;
	public int winner = -1;
	public int[] winRound;
	public int[] stepsNum;
	public int[][] scores;
	public int[][] errors;
	public long[][] timecost;
    public int [] errCounts;
    public int [] totalTime;
	public int [] invalidSteps; // 下棋出错的次数

	public ContestResult() {
		this.players = new Player[2];
		this.winRound = new int[2];
		this.stepsNum = new int[ROUNDS];
		this.scores = new int[2][ROUNDS];
		this.errors = new int[2][ROUNDS];
        this.timecost = new long[2][ROUNDS];
		this.errCounts = new int[2];
		this.totalTime = new int[2];
		this.invalidSteps = new int[2];
	}
	
	public ContestResult(int id, Player user1, Player user2) {
		this.id = id;
		this.players = new Player[2];
		this.players[0] = user1;
		this.players[1] = user2;
		this.winRound = new int[2];
		this.stepsNum = new int[ROUNDS];
		this.scores = new int[2][ROUNDS];
		this.errors = new int[2][ROUNDS];
		this.timecost = new long[2][ROUNDS];
        this.errCounts = new int[2];
        this.totalTime = new int[2];
		this.invalidSteps = new int[2];
	}
	
	public void evaluate() {
		if (winner != -1) {
			return;
		}

		errCounts[0] = errCounts[1] = 0;
		totalTime[0] = totalTime[1] = 0;
		// 每一步不合法的棋子扣1分
		winRound[0] = -1 * invalidSteps[0];
		winRound[1] = -1 * invalidSteps[1];

		for (int round = 0; round < ROUNDS; round++) {
			// 当前局黑白棋胜子的个数
			winRound[0] += scores[0][round];
			winRound[1] += scores[1][round];

			if (errors[0][round] > ERRORS) {
				winRound[1] += 5;
				continue;
			} else
				errCounts[0] += errors[0][round];
			if (errors[1][round] > ERRORS) {
				winRound[0] += 5;
				continue;
			} else
				errCounts[1] += errors[1][round];
			totalTime[0] += timecost[0][round];
			totalTime[1] += timecost[1][round];
		}
		if (winRound[0] > winRound[1]) {
			winner = 0;
		} else if (winRound[0] < winRound[1]) {
			winner = 1;
		} else {
		   	if (totalTime[0] < totalTime[1])
		        winner = 0;
		    else if (totalTime[1] < totalTime[0])
		        winner = 1;
		    else
			    winner = 2;     // tie
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("");
		sb.append("CONTEST/TEST/DEBUG ");
		sb.append(this.id);
		sb.append("\nPLAYER ");
		sb.append(this.players[0].getId());
		sb.append(" VS ");
		sb.append(this.players[1].getId());
		sb.append("\nWINNER ");
		sb.append(this.winner);
		sb.append("\nWIN_SCORE ");
		sb.append(this.winRound[0]);
		sb.append(":");
		sb.append(this.winRound[1]);
		sb.append("\nSCORE_OF_ROUND ");
		for (int r = 0; r < ROUNDS; r++) {
			sb.append(this.scores[0][r]);
			sb.append(":");
			sb.append(this.scores[1][r]);
			sb.append(" ");
		}
		sb.append("\nSTEP_NUMBER_OF_ROUND ");
		for (int r = 0; r < ROUNDS; r++) {
			sb.append(this.stepsNum[r]);
			sb.append(" ");
		}
		sb.append("\nERROR_NUMBER_OF_ROUND ");
		for (int r = 0; r < ROUNDS; r++) {
			sb.append(this.errors[0][r]);
			sb.append(":");
			sb.append(this.errors[1][r]);
			sb.append(" ");
		}
		sb.append("\nTIMECOST_OF_ROUND(ms) ");
		for (int r = 0; r < ROUNDS; r++) {
		    sb.append(this.timecost[0][r]);
		    sb.append(":");
		    sb.append(this.timecost[1][r]);
		    sb.append(" ");
        }
		return sb.toString();
	}
}
