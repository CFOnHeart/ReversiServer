package cc.cxsj.nju.reversi;

import cc.cxsj.nju.reversi.communication.ClientConnectMonitor;
import cc.cxsj.nju.reversi.config.ServerProperties;
import cc.cxsj.nju.reversi.info.Player;
import cc.cxsj.nju.reversi.info.Players;
import cc.cxsj.nju.reversi.task.CreateServiceRunnable;
import cc.cxsj.nju.reversi.ui.MainFrame;
import cc.cxsj.nju.reversi.ui.SwingConsole;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Main {

    static {
        PropertyConfigurator.configure(System.getProperty("user.dir") + "/config/log4j.properties");
        System.setProperty("file.encoding", "UTF-8");
    }

    private static final Logger LOG = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        // read config
        ServerProperties.instance();

        // start ui
        // 开启一个ui的多线程
        LOG.info("Startup the main ui");
        SwingConsole.run(MainFrame.instance());

        // print server propeties in ui
        ServerProperties.printServerProperties();

        // load users
        Players.loadPlayers();

        // prepare creating service
        CreateServiceRunnable.instance().start();

        // monitor connect
        ClientConnectMonitor.instance().start();

    }
}
