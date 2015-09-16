package core.scheduler;

import core.Page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.*;

/**
 * Created by LaoJia on 2015/9/13.
 */
/*
页面解析请求调度器
 */
public class ParserScheduler implements Scheduler<Page>{

    private BlockingQueue<Page> unProcQueue;//URL和网页内容
    private Map<String, String> pageMap = new HashMap<String, String>();
    private int MAX_SIZE = 20;
    private int unProcSize = 100;
    private ArrayList<String> procList;//需不需要并发的集合
    private ReadWriteLock rwlock;//ReentrantReadWriteLock
    private Lock lock;
    private Condition full;
    private Condition empty;

    //单例模式，保证Scheduler的唯一性
    private static class ParserSchedulerHolder{
        private static final ParserScheduler INSTANCE = new ParserScheduler();
    }
    private ParserScheduler(){
        unProcQueue = new LinkedBlockingQueue<Page>(MAX_SIZE);
        procList = new ArrayList<String>();
        rwlock = new ReentrantReadWriteLock();
        lock = new ReentrantLock();
        full = lock.newCondition();
        empty = lock.newCondition();
    }
    public static final ParserScheduler getInstance(){
        return ParserSchedulerHolder.INSTANCE;
    }

    //生产者消费者模式，生产和消费URL
    public void push(Page page){
        if(unProcQueue.remainingCapacity() == 0) {
            System.out.println(Thread.currentThread().getName() + ":page queue is full.");
        }

        //判断页面是否重复

        try {
            unProcQueue.put(page);
            System.out.println(Thread.currentThread().getName() + ": push page:" + page.getWebsite() + " to page queue");
            //System.out.println("push new page " + page.getWebsite() +" to page queue");
        }catch(InterruptedException ie){
            ie.printStackTrace();
        }

    }

    public Page pop(){
        if(unProcQueue.size() == 0) {
            System.out.println(Thread.currentThread().getName() + ":page queue is empty...");
        }
        Page page = null;
        try{
            page = unProcQueue.take();
            System.out.println(Thread.currentThread().getName() + ": pop page:" + page.getWebsite() + " from page queue");
            return page;
        }catch(InterruptedException ie){
            ie.printStackTrace();
        }

        return page;
    }

    //读者写者模式，将已处理的URL队列加入队列
    public void writeDealedRequest(String url){
        rwlock.writeLock().lock();
        procList.add(url);
        rwlock.writeLock().unlock();
    }
    //判断URL是否在Dealed队列中
    public boolean readDealedRequest(String url){
        //if()
        rwlock.readLock().lock();
        if(procList.contains(url)){
            rwlock.readLock().unlock();
            return true;
        }
        rwlock.readLock().unlock();
        return false;

    }
}
