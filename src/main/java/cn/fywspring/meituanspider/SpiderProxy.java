package cn.fywspring.meituanspider;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

/**
 * Created by yiwan on 17-7-20.
 */
public class SpiderProxy {
    private static final Logger log = Logger.getLogger(SpiderProxy.class);

    //从http://www.xicidaili.com/获取代理ip地址，并存入ip.propertites
    public static void getProxyIpAndPort() throws Exception{
        PropertyConfigurator.configure("log4j.properties");
        int count = 0;
        Map<String,String> map = new HashMap<String, String>();
        Connection connection = null;
        RWProperties.clearProp("ip.properties");
        for (int i = 1; i < 11; i++) {
            connection = Jsoup.connect("http://www.xicidaili.com/wt/"+i).userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:45.0) Gecko/20100101 Firefox/45.0");
            Elements speedEles = connection.get().select("table tbody tr td:eq(6) div[title]");
            for (Element speedEle:
                    speedEles) {
                //过滤出速度<1s的ip地址和端口号port
                String speedStr = speedEle.attr("title");
                Double speed = Double.parseDouble(speedStr.substring(0,speedStr.length()-1));
                if (speed<1.0) {
                    count++;
                    map.put("ip_"+count,speedEle.parent().parent().children().get(1).text());
                    map.put("port_"+count,speedEle.parent().parent().children().get(2).text());
                }
            }
        }
        RWProperties.writeProp("ip.properties",map);
        RWProperties.close();
        log.debug("本次一共获取到" + count + "个ip地址和端口号！已存入ip.properties");
    }

    //用代理ip去访问美团
    public static Document getProxy(String url) {
        String fileName = "ip.properties";
        String[] keys = new String[2];
        try {
            PropertyConfigurator.configure("log4j.properties");
            String ip_port = getIP();
            String ipKey = ip_port.split(":")[0];
            String ip = ip_port.split(":")[1];
            String portKey = ip_port.split(":")[2];
            String port = ip_port.split(":")[3];
            keys[0] = ipKey;
            keys[1] = portKey;
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, Integer.valueOf(port)));

            Connection connection = Jsoup.connect(url);
            connection.header("Host","bj.meituan.com");
            connection.header("User-Agent","Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:54.0) Gecko/20100101 Firefox/54.0");
            connection.header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.header("Accept-Language","en-US,en;q=0.5");
            connection.header("Accept-Encoding","gzip, deflate");
            connection.header("Cookie", String.valueOf(connection.response().cookies()));
            connection.header("Connection","keep-alive");
            connection.header("Upgrade-Insecure-Requests","1");
            connection.method(Connection.Method.GET);
            connection.proxy(proxy);
            connection.timeout(6000);
            Document document = connection.get();
            return document;
        } catch (Exception e){
            log.debug(e.getMessage());
            if ("拒绝连接".equals(e.getMessage())) {
                RWProperties.removeProp(fileName,keys);
            }
        }
        return null;
    }

    public static String getIP(){
        String fileName = "ip.properties";
        Properties properties = RWProperties.readProp(fileName);

        int ran = new Random().nextInt(properties.values().size()/2) + 1;
        String ipKey = "ip_"+ran;
        String portKey = "port_"+ran;
        String ip = properties.getProperty(ipKey);
        String port  = properties.getProperty(portKey);
        if (null == ip){
            getIP();
        }
        return new String(ipKey+":"+ip+":"+portKey+":"+port);
    }

}
