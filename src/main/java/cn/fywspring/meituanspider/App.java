package cn.fywspring.meituanspider;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 爬虫程序入口
 */
public class App {
    public static final Logger log = Logger.getLogger(App.class);
    private static List<String> list = null;

    public static void main(String[] args) {
        final long start = System.currentTimeMillis();
        //加载日志配置
        PropertyConfigurator.configure("log4j.properties");
        log.debug(Thread.currentThread().getName() + "我是主线程！");
        try {
            ReentrantLock lock = new ReentrantLock();
            //初始化代理ip
            SpiderProxy.getProxyIpAndPort();
            //线程池
            ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

            //开启一个爬取所有城区的线程
            MTSpider spider = new MTSpider();
            Future<List<String>> list = cachedThreadPool.submit(new ZoneGetter(spider));
            log.debug("成功返回list：" + list.get());
            //放入队列

//            cachedThreadPool.execute(new MQSend(MQConnection.getConnection(),"zone_queue_work",list.get()));
            List<String> strings = new ArrayList<String>();
            strings = list.get();
            cachedThreadPool.execute(new MQSend("test_queue",strings,lock));

//            lock.lock();
//            for (int i = 0; i < list.get().size(); i++) {
////                cachedThreadPool.submit(new BCGetter(spider, list.get().get(i)));
//                cachedThreadPool.execute(new MQConsumer(MQConnection.getConnection(),"test_queue"));
//            }

            cachedThreadPool.shutdown();
//            lock.unlock();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //指定一个城市的url
//        String cityUrl = "http://bj.meituan.com/category/meishi";
//        log.debug("正在爬取所有城区……");
//        getZones(cityUrl);

        //log.debug("正在爬取所有商圈……");
        //String zoneUrl = "http://bj.meituan.com/category/meishi/chaoyangqu";
        //getBC(zoneUrl);

        //log.debug("正在爬取所有店铺……");
        //String BCUrl = "http://bj.meituan.com/category/meishi/anzhen";
        //getShops(BCUrl);

        //log.debug("正在爬取店铺内所有信息……");
        //String shopUrl = "http://bj.meituan.com/shop/4399987";
        //getShopDetail(shopUrl);
    }


}
