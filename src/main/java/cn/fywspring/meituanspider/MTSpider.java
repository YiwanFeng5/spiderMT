package cn.fywspring.meituanspider;

/**
 * Created by yiwan on 17-7-21.
 */
public class MTSpider {
    private boolean isDone = false;//true:执行完成    false正在执行

    public synchronized void working() throws InterruptedException {
        wait();
    }

    public synchronized void worked(){
        isDone = true;
        notifyAll();
    }
}
