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
ҳ��������������
 */
public class ParserScheduler implements Scheduler<Page>{

    private BlockingQueue<Page> unProcQueue;//URL����ҳ����
    private Map<String, String> pageMap = new HashMap<String, String>();
    private int MAX_SIZE = 20;
    private int unProcSize = 100;
    private ArrayList<String> procList;//�費��Ҫ�����ļ���
    private ReadWriteLock rwlock;//ReentrantReadWriteLock
    private Lock lock;
    private Condition full;
    private Condition empty;

    //����ģʽ����֤Scheduler��Ψһ��
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

    //������������ģʽ������������URL
    public void push(Page page){
        if(unProcQueue.remainingCapacity() == 0) {
            System.out.println(Thread.currentThread().getName() + ":page queue is full.");
        }

        //�ж�ҳ���Ƿ��ظ�

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

    //����д��ģʽ�����Ѵ����URL���м������
    public void writeDealedRequest(String url){
        rwlock.writeLock().lock();
        procList.add(url);
        rwlock.writeLock().unlock();
    }
    //�ж�URL�Ƿ���Dealed������
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
