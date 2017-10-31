package cn.fywspring.meituanspider;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Created by yiwan on 17-7-26.
 */
public class MQSend implements Runnable {
    private String QUEUE_NAME;
    private List<String> urls;
    private Lock lock;
    public MQSend(String QUEUE_NAME,List<String> urls,Lock lock){
        this.QUEUE_NAME = QUEUE_NAME;
        this.urls = urls;
        this.lock = lock;
    }

    public void run() {
        Connection connection = null;
        Channel channel = null;
        try {
            lock.lock();
            connection = MQConnection.getConnection();
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            if(!urls.isEmpty()) {
                for(int i = 0; i < urls.size(); i++) {
                    channel.basicPublish("", QUEUE_NAME, null, urls.get(i).getBytes());
                }
                System.out.println(Thread.currentThread().getName()+"已经入队");
            }
            lock.unlock();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(null != channel) {
                try {
                    channel.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    channel = null;
                }
            }

            if(null != connection) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    connection = null;
                }
            }

        }
    }
}
