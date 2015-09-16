package core.scheduler;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by LaoJia on 2015/9/13.
 */
/*
使用Java中的阻塞队列来实现对URL的管理
请求的页面地址交由URL阻塞队列来调度

全局只有一个，单例模式

 */
public class RequestScheduler implements Scheduler<String>{


    private BlockingQueue<String> unProcQueue;//未处理请求队列  生产者-消费者模式
    private int unProcSize = 100;
    //已经处理请求队列  这个不能用限制容量的队列  而且访问模式也不同  需要可以同时读取，写操作不可以冲突
    //采用可以快速检索的数据结构，还需要并发控制吗？读操作同时进行 不会改变数据   写操作与读操作分离  写操作之间也是分离的
    //private //读者写者模式
    private ArrayList<String> procList;//需不需要并发的集合
    //private BlockingQueue<String> procQueue;
    //private int procSize = 200;
    private ReadWriteLock lock;//ReentrantReadWriteLock


    //单例模式，保证Scheduler的唯一性
    private static class RequestSchedulerHolder{
        private static final RequestScheduler INSTANCE = new RequestScheduler();
    }
    private RequestScheduler(){
        unProcQueue = new LinkedBlockingQueue<String>(unProcSize);
        procList = new ArrayList<String>();
        lock = new ReentrantReadWriteLock();
    }
    public static final RequestScheduler getInstance(){
        return RequestSchedulerHolder.INSTANCE;
    }

    //生产者消费者模式，生产和消费URL
    public void push(String url){
        if(unProcQueue.remainingCapacity() == 0){
            System.out.println(Thread.currentThread().getName() + ": request queue is full...");
        }

        //判断URL是否重复

        try {
            unProcQueue.put(url);
            System.out.println(Thread.currentThread().getName() + ": push website:" + url + " to request queue.");
            //System.out.println("push url to unProcQueue...");
        }catch(InterruptedException ie){
            ie.printStackTrace();
        }
    }
    public String pop(){
        if(unProcQueue.size() == 0){
            System.out.println(Thread.currentThread().getName() + ": request queue is empty.");
            //System.out.println("Undealed request queue is empty...");
        }
        try{
            String url = unProcQueue.take();
            System.out.println(Thread.currentThread().getName() + ": pop website:" + url + " from request queue.");
            return url;
        }catch(InterruptedException ie){
            ie.printStackTrace();
        }
        return null;
    }

    //读者写者模式，将已处理的URL队列加入队列
    public void writeDealedRequest(String url){
        lock.writeLock().lock();
        procList.add(url);
        lock.writeLock().unlock();
    }
    //判断URL是否在Dealed队列中
    public boolean readDealedRequest(String url){
        //if()
        lock.readLock().lock();
        if(procList.contains(url)){
            lock.readLock().unlock();
            return true;
        }
        lock.readLock().unlock();
        return false;

    }

}
