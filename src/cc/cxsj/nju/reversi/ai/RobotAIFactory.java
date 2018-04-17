package cc.cxsj.nju.reversi.ai;

import cc.cxsj.nju.reversi.Main;
import org.apache.log4j.Logger;

public class RobotAIFactory {
	
	private static final Logger LOG = Logger.getLogger(Main.class);
	
	public static ReversiRobotAI produceRobotAIof(RobotAIModel model) {
		switch (model) {
			case RobotMoreChess:
                LOG.info("Produce Robot Omega");
                return new ReversiRobotAIMoreChessOneStep();
			case RobotAlpha:
				LOG.info("Produce one Robot Alpha");
			//	return new RobotI();
			case RobotBeta:
				LOG.info("Produce one Robot Beta");
			//	return new RobotII();
			case RobotGamma:
				LOG.info("Produce one Robot Gamma");
			//	return new RobotO(2);
            case RobotLambda:
            	break;
            case RobotTravel:
            	LOG.info("Produce one Robot Travel");
            	return new ReversiRobotAITravel();
			default:
				LOG.info("Robot Factory can not produce this model Robot!");
				System.exit(0);
			}
		return null;
	}
}
