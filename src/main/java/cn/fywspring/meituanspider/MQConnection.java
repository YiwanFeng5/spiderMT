package cn.fywspring.meituanspider;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by yiwan on 17-7-26.
 */
public class MQConnection {

    public static Connection getConnection(){
        try {
            Properties properties = new Properties();
            FileInputStream is = new FileInputStream("rabbitmqconf.properties");
            properties.load(is);
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(properties.getProperty("rabbit.ip"));
            factory.setPort(Integer.valueOf(properties.getProperty("rabbit.port")).intValue());
            factory.setVirtualHost(properties.getProperty("rabbit.vhost"));
            factory.setUsername(properties.getProperty("rabbit.username"));
            factory.setPassword(properties.getProperty("rabbit.password"));
            Connection connection = factory.newConnection();
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
