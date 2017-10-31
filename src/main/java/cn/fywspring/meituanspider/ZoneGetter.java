package cn.fywspring.meituanspider;


import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.Callable;


/**
 * Created by yiwan on 17-7-21.
 */
public class ZoneGetter implements Callable {
    private MTSpider mtSpider;
    private static final Logger log = Logger.getLogger(ZoneGetter.class);
    public ZoneGetter(MTSpider spider) {
        mtSpider = spider;
    }

    public List<String> call() throws Exception {
        long start = System.currentTimeMillis();
        int failCount = 0;
        int failReqTimes = 0;
        List<String> list;
        log.debug(Thread.currentThread().getName() + "正在爬所有城区……");
        while (true) {
            try {
                String cityUrl = RWProperties.readProp("baseUrl.properties").getProperty("baseUrl");
                if (failCount > 0) {
                    log.debug("请求失败" + failCount + "次");
                }
                if (failCount > 250) {
                    log.debug("请求失败超过250次，正在更新ip.properties");
                    failCount = 0;
                    SpiderProxy.getProxyIpAndPort();
                    continue;
                }
                list = SpiderUtils.getZones(cityUrl);
                //mtSpider.wait(60000);
                log.debug("耗时：" + (System.currentTimeMillis() - start) / 1000.0 + "s" + "\t失败总次数：" + failReqTimes);
                break;
            } catch (Exception e) {
                log.debug(Thread.currentThread().getName()+" --> "+e.getMessage());
                failCount++;
                failReqTimes++;
                try {
                    Thread.sleep(1000);
                } catch (Exception e1) {
                    log.debug(e1.getMessage());
                }
                continue;
            }
        }
        return list;
    }
}
