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
�������������ݳ־û�������
 */
public class PersistenceScheduler implements Scheduler<Outcome>{

    private BlockingQueue<Outcome> persistQueue;//URL����ҳ����

    private int persistSize = 100;
    //�Ѿ������������  ������������������Ķ���  ���ҷ���ģʽҲ��ͬ  ��Ҫ����ͬʱ��ȡ��д���������Գ�ͻ
    //���ÿ��Կ��ټ��������ݽṹ������Ҫ���������𣿶�����ͬʱ���� ����ı�����   д���������������  д����֮��Ҳ�Ƿ����
    //private ����д��ģʽ
    private ArrayList<String> procList;//�費��Ҫ�����ļ���
    //private BlockingQueue<String> procQueue;
    //private int procSize = 200;
    private ReadWriteLock lock;//ReentrantReadWriteLock


    //����ģʽ����֤Scheduler��Ψһ��
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

    //������������ģʽ������������URL
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

    //����д��ģʽ�����Ѵ����URL���м������
    public void writeDealedRequest(String url){
        lock.writeLock().lock();
        procList.add(url);
        lock.writeLock().unlock();
    }
    //�ж�URL�Ƿ���Dealed������
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
