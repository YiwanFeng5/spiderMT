package cn.fywspring.meituanspider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yiwan on 17-7-21.
 */
public class SpiderUtils {

    public static final Logger log = Logger.getLogger(App.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    //爬取所有城区的url
    public static List<String> getZones(String cityUrl) throws Exception{
        List<String> list = new ArrayList<String>();
        //使用代理
        //Document document = SpiderProxy.getProxy(cityUrl);
        //不使用代理
        Document document = getDoc(cityUrl);
        Elements zones = document.select("ul li[class=item] a");
        if (zones != null) {
            for (Element zone:
                    zones) {
                if (zone.attr("href").matches("("+cityUrl+"/)([a-z]+)+") && !(zone.text().equals("地铁附近"))){
                    log.debug(zone.text() + " : " + zone.attr("href"));
                    list.add(zone.attr("href"));
                }
            }
        }
        return list;
    }

    //爬取指定城区下的所有商圈url
    public static void getBC(String zoneUrl) throws Exception{
        String cityUrl = zoneUrl.substring(0,zoneUrl.lastIndexOf("/")+1);
        //使用代理
        //Document document = SpiderProxy.getProxy(zoneUrl);
        //不使用代理
        Document document = getDoc(zoneUrl);
        Elements BCs = document.select("ul li[class^=item] a");
        if (BCs != null) {
            for (Element BC:
                    BCs) {
                if (BC.attr("href").matches("("+cityUrl+")([a-z]+)+")
                        && !BC.text().startsWith("全部")) {
                    log.debug(BC.text()+" : "+BC.attr("href"));
                }
            }
        }
    }

    //爬取指定商圈下的所有的商铺
    public static void getShops(String BCUrl) throws Exception{
        Document document = getDoc(BCUrl);
        //获取页数element.size()-1,li的个数-1页
        Elements elements = document.select("div[class=paginator-wrapper] ul li");
        if (elements != null) {
            //临时存储页数
            int pageCount = elements.size()-1;
            log.debug("共" + pageCount + "页");
            //遍历每一页的店铺
            for (int i = 1; i <= pageCount; i++) {
                log.debug("正在爬取第" + i + "页");
                //指定商区下的所有店铺页面
                //document = Jsoup.connect(BCUrl+"/page"+i).userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:45.0) Gecko/20100101 Firefox/45.0").get();
                getDoc("BCUrl+\"/page\"+i");
                //确定存放json传的div，该json中含有当前页的所有的商店id
                Element element = document.select("div[data-async-params]").first();
                if (element != null) {
                    //因为输出的json串有点偏差，json嵌套不正确，导致不能正确解析，所以在此进行修正
                    String shopsJson = element.attr("data-async-params").replace("\\\"", "\"").replace("\"{", "{").replace("}\"", "}");
                    //通过json工具类获取商店数组
                    String poiids = MAPPER.readTree(shopsJson).get("data").get("poiidList").toString();
                    //对数组进行处理
                    String poiidArrayStr = poiids.substring(1, poiids.length()-1).trim();//去除左右的方括号[]
                    String[] poiidArray = poiidArrayStr.split(",");//将字符串拆开，然后放入集合
                    /**
                     * 根据美团的商店链接来拼串，http://bj.meituan.com/shop/page/+i(页数)+/poiid(商店id)，
                     * 例如http://bj.meituan.com/shop/page+1+/99228003，
                     * 然后将其存入list中
                     */
                    for (String poiid : poiidArray) {
                        log.debug("http://bj.meituan.com/shop/"+poiid);
                    }
                }
            }
        }
    }

    //爬取指定店铺的所有信息
    public static void getShopDetail(String shopUrl) throws Exception {
        //评价总数
        int count = 0;
        //截取店铺id
        String shopID = shopUrl.substring(shopUrl.lastIndexOf("/")+1);
        //根据商店链接获取Document对象
        Document doc = getDoc(shopUrl);
        // 1.抓取商家信息
        log.debug("抓取商家信息中……");
        String shopName = doc.select("div h2 span").get(0).text();
        log.debug("商家店名：" + shopName);
        String shopAddr = doc.select("div p span").get(0).text();
        log.debug("商家地址：" + shopAddr);
        String shopTel = doc.select("div[class=fs-section__left] p").get(1).text();
        log.debug("商家电话：" + shopTel);
        String shopScore = doc.select("div div span strong").get(0).text();
        log.debug("商家评分：" + shopScore);
        String shopTag = doc.select("a[class=tag]").get(0).text();
        log.debug("商家标签：" + shopTag);
        String shopRateCount = doc.select("a[class=num rate-count]").get(0).text();
        log.debug("评价人数：" + shopRateCount);
        // 2.抓取正在团购
        log.debug("抓取商家团购信息……");
        Elements elesCF = doc.select("li[class^=item cf]");
        log.debug("共" + elesCF.size() + "个团购信息：");
        for (Element ele : elesCF) {
            String cfImgUrl = ele.select("a[class=item__title] img").attr("data-src");
            log.debug("图片地址：" + cfImgUrl);
            String cfTitle = ele.select("a span[class=title]").text();
            log.debug("项目标题：" + cfTitle);
            String cfSale = ele.select("a span[class=sale]").text();
            log.debug("已售：" + cfSale);
            String cfDuedate = ele.select("span[class=item__duedate] span").text();
            log.debug("截止日期：" + cfDuedate);
            String cfPrice = ele.select("strong").text();
            log.debug("价格：" + cfPrice);
            String cfDelPrice = ele.select("span span del").text();
            log.debug("门店价：" + cfDelPrice);
        }
        // 3.抓取商家详情
        log.debug("开始爬取商家详情……");
        Elements detailItems = doc.select("div[class=field-group]");
        for (Element ele : detailItems) {
            log.debug(ele.text());
        }
        // 4.抓取消费评价
        //4.1获取评价总页数
        log.debug("开始爬取用户评价……");

        //根据data-total来算出总页数
        Integer pages = Integer.valueOf(doc.select("div div[data-total]").attr("data-total"));
        if (pages%10>0) {
            pages = pages/10 +1;
        } else {
            pages /= 10;
        }
        log.debug("总页数：" + pages);
        String reviewsJson = null;
        for (int i = 0; i < pages; i++) {
            if (0==i) {
                reviewsJson = Jsoup.connect("http://bj.meituan.com/deal/feedbacklist/0/"+shopID+"/all/0/default/1?limit=10&showpoititle=0&offset=0").ignoreContentType(true).execute().body();
            } else {
                reviewsJson = Jsoup.connect("http://bj.meituan.com/deal/feedbacklist/0/"+shopID+"/all/0/default/1?limit=10&showpoititle=0&offset="+i+"0").ignoreContentType(true).execute().body();
            }
            String str = MAPPER.readTree(reviewsJson).get("data").get("ratelistHtml").toString().replace("\\\"","\"");
            Document reviewsDoc = Jsoup.parse(str.substring(1,str.length()-1));
            Elements reviews = reviewsDoc.select("li[data-rateid]");
            log.debug(reviews.size());
            for (Element ele : reviews) {
                String userImage = ele.select("div[class=user-info-block] div img[class=avatar]").get(0).attr("src");
                log.debug("用户头像："+userImage);
                String userName = ele.select("span[class^=name").get(0).text();
                log.debug("用户名："+userName);
                String userLevel = ele.select("span[class=growth-info] i").get(0).attr("title");
                log.debug("用户等级："+userLevel);
                String rate_starts = ele.select("span[class=rate-stars]").get(0).attr("style");
                log.debug("评价星级："+rate_starts);
                String time = ele.select("span[class=time]").get(0).text();
                log.debug("评价时间："+time);
                String content = ele.select("div p[class=content]").get(0).text().replace("\\n","");
                log.debug("评价内容："+content);
                String deal = ele.select("p[class=deal-title] a").get(0).text();
                log.debug("评价套餐："+deal);
                count++;
            }
        }
        log.debug("总评价数："+count);
    }

    //公共的获取Document的方法
    public static Document getDoc(String url) throws Exception{
        Connection connection = Jsoup.connect(url);
        connection.header("Host","bj.meituan.com");
        connection.header("User-Agent","Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:54.0) Gecko/20100101 Firefox/54.0");
        connection.header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.header("Accept-Language","en-US,en;q=0.5");
        connection.header("Accept-Encoding","gzip, deflate");
        connection.header("Cookie", String.valueOf(connection.response().cookies()));
        connection.header("Connection","keep-alive");
        connection.header("Upgrade-Insecure-Requests","1");
//        connection.userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:54.0) Gecko/20100101 Firefox/54.0");
//        connection.cookies(connection.response().cookies());
        connection.method(Connection.Method.GET);
        //connection.timeout(10000);
        Document document = connection.get();
        return document;
    }
}
