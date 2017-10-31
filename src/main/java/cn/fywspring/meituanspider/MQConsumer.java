package cn.fywspring.meituanspider;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import java.util.concurrent.locks.Lock;

/**
 * Created by yiwan on 17-7-26.
 */
public class MQConsumer implements Runnable{

    private String QUEUE_NAME;
    private Lock lock;
    public MQConsumer(String QUEUE_NAME,Lock lock){
        this.QUEUE_NAME = QUEUE_NAME;
        this.lock = lock;
    }

    public void run() {
        Connection connection = null;
        Channel channel = null;
        try {
            System.out.println(Thread.currentThread().getName()+"开始工作");
            connection = MQConnection.getConnection();
            channel = connection.createChannel();
            //申明队列
            channel.queueDeclare(QUEUE_NAME,false,false,false,null);
            //同一时刻服务器只会发送一条消息给消费者，每一次服务器只会向客户端发送一条
            channel.basicQos(1);

            //定义队列的消费者
            QueueingConsumer consumer = new QueueingConsumer(channel);
            //监听队列，手动返回完成
            channel.basicConsume(QUEUE_NAME,false,consumer);

            //获取消息
            while(true){
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                String message = new String(delivery.getBody());
                System.out.println("[x]Sent'"+message+"'");
                new BCGetter(new MTSpider(),message);
                //休眠
                Thread.sleep(10);
                //返回确认状态
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
