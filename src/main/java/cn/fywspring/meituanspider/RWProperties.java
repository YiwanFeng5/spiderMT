package cn.fywspring.meituanspider;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 用来操作properties的类
 * Created by yiwan on 17-7-20.
 */
public class RWProperties {
    //记录日志
    private static final Logger log = Logger.getLogger(RWProperties.class);
    //写出Properties的一个文件输出流
    private static FileOutputStream fos = null;
    //读入Properties的一个文件输入流
    private static InputStream is = null;

    //写出一个properties
    public static void writeProp(String fileName, Map<String,String> map){
        try {
            Properties properties = new Properties();
            fos = new FileOutputStream(fileName,true);
            properties.putAll(map);
            properties.store(fos,null);
        } catch (Exception e) {
            log.debug("写入properties失败！"+e.getMessage());
        }
    }

    //读入一个properties
    public static Properties readProp(String fileName){
        try {
            Properties properties = new Properties();
            is = new BufferedInputStream(new FileInputStream(fileName));
            if (null != properties){
                properties.load(is);
                return properties;
            }
        } catch (IOException e) {
            log.debug("文件读入异常！" + e.getMessage());
        }
        return null;
    }

    public static void clearProp(String fileName){
        try {
            Properties properties = new Properties();
            Map<String, String> map = new HashMap<String, String>();
            is = new BufferedInputStream(new FileInputStream(fileName));
            fos = new FileOutputStream(fileName);
            if (null != properties){
                properties.load(is);
                properties.clear();
                properties.store(fos,"");
            }
        } catch (IOException e) {
            log.debug("文件读入异常！" + e.getMessage());
        }
    }

    //删除请求失败次数>3的ip和对应的port
    @Test
    public static void removeProp(String fileName, String[] keys){
        try {
            Properties properties = new Properties();
            is = new BufferedInputStream(new FileInputStream(fileName));
            properties.load(is);
            for (int i = 0; i < keys.length; i++) {
                properties.remove(keys[i]);
            }
            fos = new FileOutputStream(fileName);
            properties.store(fos,null);
            log.debug("已将"+ Arrays.toString(keys)+"从"+fileName+"中删除！");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void close(){
        if (null != fos){
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                fos = null;
            }
        }
        if (null != is){
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                is = null;
            }
        }
    }

    @Test
    public void test(){
        String fileName = "ip.properties";
        readProp(fileName);
    }
}
