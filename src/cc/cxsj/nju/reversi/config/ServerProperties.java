package cc.cxsj.nju.reversi.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

import cc.cxsj.nju.reversi.Main;
import cc.cxsj.nju.reversi.ui.MainFrame;

/**
 * server config
 * 
 * @author coldcode
 *
 */
public class ServerProperties {

	private static final Logger LOG = Logger.getLogger(Main.class);
	
	private static Properties properties = null;

	public static Properties instance() {  // 读取服务器属性
		if (properties == null) {
			synchronized (Properties.class) {
				if (properties == null) {
					properties = new Properties();
					try {
						// System.getProperty("user.dir") 表示加载当前的工作目录
						FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "/config/server.properties");
						properties.load(fis);
						fis.close();
					} catch (FileNotFoundException e) {
						LOG.error("Not found server.properties file");
						System.exit(0);
					} catch (IOException e) {
						LOG.error("Error when load server.properties file");
						System.exit(0);
					}
					
					LOG.info("Read server.properties complete! The properties as follows: ");
					for (Entry<Object, Object> entry : properties.entrySet()) {
						LOG.info(entry.getKey() + "=" + entry.getValue());
					}
				}
			}
		}
		return properties;
	}

	public static void printServerProperties() {
		MainFrame.instance().log("The server properties as follows: ");
		for (Entry<Object, Object> entry : properties.entrySet()) {
			MainFrame.instance().log(entry.getKey() + "=" + entry.getValue());
		}
	}
	
}
