package core.scheduler;

import core.Outcome;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by LaoJia on 2015/9/13.
 */
/*
解析出来的数据持久化调度器
 */
public class PersistenceScheduler implements Scheduler<Outcome>{

    private BlockingQueue<Outcome> persistQueue;//URL和网页内容

    private int persistSize = 100;
    //已经处理请求队列  这个不能用限制容量的队列  而且访问模式也不同  需要可以同时读取，写操作不可以冲突
    //采用可以快速检索的数据结构，还需要并发控制吗？读操作同时进行 不会改变数据   写操作与读操作分离  写操作之间也是分离的
    //private 读者写者模式
    private ArrayList<String> procList;//需不需要并发的集合
    //private BlockingQueue<String> procQueue;
    //private int procSize = 200;
    private ReadWriteLock lock;//ReentrantReadWriteLock


    //单例模式，保证Scheduler的唯一性
    private static class PersistenceSchedulerHolder{
        private static final PersistenceScheduler INSTANCE = new PersistenceScheduler();
    }
    private PersistenceScheduler(){
        persistQueue = new LinkedBlockingQueue<Outcome>(persistSize);
        procList = new ArrayList<String>();
        lock = new ReentrantReadWriteLock();
    }
    public static final PersistenceScheduler getInstance(){
        return PersistenceSchedulerHolder.INSTANCE;
    }

    //生产者消费者模式，生产和消费URL
    public void push(Outcome out){
        if(persistQueue.remainingCapacity() == 0){
            System.out.println(Thread.currentThread().getName() + ":persistence queue is full...");
        }
        //for(int i = 0 ; i < )
        try {
            persistQueue.put(out);
            System.out.println(Thread.currentThread().getName() + ":push outcome " + out.getWebsite() + " to persistQueue...");
        }catch(InterruptedException ie){
            ie.printStackTrace();
        }
    }
    public Outcome pop(){
        if(persistQueue.size() == 0){
            System.out.println(Thread.currentThread().getName() + ":persistence queue is empty...");
        }
        Outcome out = null;
        try{
            out = persistQueue.take();
            System.out.println(Thread.currentThread().getName() + ":pop outcome " + out.getWebsite() + " from persistQueue...");
            return out;
        }catch(InterruptedException ie){
            ie.printStackTrace();
        }
        return out;
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
