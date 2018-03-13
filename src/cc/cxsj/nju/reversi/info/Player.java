package cc.cxsj.nju.reversi.info;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class Player {
	private String id;
	private String password;
	private String name;
	private Socket socket = null;
	private BufferedInputStream receiver = null;
	private OutputStream sender = null;
	
	protected byte[] sendBuffer = new byte[16];
	protected byte[] recvBuffer = new byte[16];

	private int punishmentScore; // 惩罚分数(超时, 下了不合法棋子)
	private long costTime; // 下棋花费的时间统计

	public Player(String id, String password, String name) {
		this.id = id;
		this.password = password;
		if (name == null) this.name = "";
		else this.name = name;
		punishmentScore = 0;
	}
	
	public String getId() {
		return id;
	}

	public String getName() { return name; }
	
	public void setId(String id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPunishment(){ return punishmentScore; }

	public void setPunishmentScore(int punishmentScore){ this.punishmentScore = punishmentScore; }

	public void punish(int score){ punishmentScore+=score; }

	public long getCostTime(){ return costTime; }

	public void setCostTime(long costTime) { this.costTime = costTime; }

	public void addCostTime(long time) { this.costTime += time; }

	public void initial(Socket socket, BufferedInputStream bfin) throws IOException{
		this.socket = socket;
		this.receiver = bfin;
		this.sender = socket.getOutputStream();
	}
	
	public byte[] receive() throws SocketTimeoutException, IOException {
		Arrays.fill(recvBuffer, (byte)0);
		this.receiver.read(recvBuffer);
		return recvBuffer;
	}

	public boolean isclosed() {
	    if (!this.socket.isConnected()) return true;
	    if (this.socket.isOutputShutdown()) return true;
	    if (this.socket.isClosed()) return true;
	    return false;
    }

	public void send(String msg) throws IOException {
		Arrays.fill(sendBuffer, (byte)0);
		byte[] bmsg = msg.getBytes();
		System.arraycopy(bmsg, 0, sendBuffer, 0, bmsg.length);
		this.sender.write(sendBuffer);
		this.sender.flush();
	}
	
	public void clear() {
		try {
			if (this.receiver != null) this.receiver.close();
			if (this.sender != null) this.sender.close();
			if (this.socket != null) this.socket.close();
		} catch (IOException e) {
			
		} finally {
			this.receiver = null;
			this.sender = null;
			this.socket = null;
		}
	}
	
	@Override
	public String toString() {
		return this.id;
	}
}
