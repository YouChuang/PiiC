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
ʹ��Java�е�����������ʵ�ֶ�URL�Ĺ���
�����ҳ���ַ����URL��������������

ȫ��ֻ��һ��������ģʽ

 */
public class RequestScheduler implements Scheduler<String>{


    private BlockingQueue<String> unProcQueue;//δ�����������  ������-������ģʽ
    private int unProcSize = 100;
    //�Ѿ������������  ������������������Ķ���  ���ҷ���ģʽҲ��ͬ  ��Ҫ����ͬʱ��ȡ��д���������Գ�ͻ
    //���ÿ��Կ��ټ��������ݽṹ������Ҫ���������𣿶�����ͬʱ���� ����ı�����   д���������������  д����֮��Ҳ�Ƿ����
    //private //����д��ģʽ
    private ArrayList<String> procList;//�費��Ҫ�����ļ���
    //private BlockingQueue<String> procQueue;
    //private int procSize = 200;
    private ReadWriteLock lock;//ReentrantReadWriteLock


    //����ģʽ����֤Scheduler��Ψһ��
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

    //������������ģʽ������������URL
    public void push(String url){
        if(unProcQueue.remainingCapacity() == 0){
            System.out.println(Thread.currentThread().getName() + ": request queue is full...");
        }

        //�ж�URL�Ƿ��ظ�

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
