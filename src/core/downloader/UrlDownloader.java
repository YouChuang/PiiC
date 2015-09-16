package core.downloader;

import core.Page;
import core.scheduler.ParserScheduler;
import core.scheduler.RequestScheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by LaoJia on 2015/9/13.
 */
/*

Java本身自带的URLConnection实现抓取页面的基本功能
 */
public class UrlDownloader implements Runnable{
    private RequestScheduler requestScheduler;
    private ParserScheduler parserScheduler;
    private String website;
    public UrlDownloader(RequestScheduler requestScheduler, ParserScheduler parserScheduler){
        this.requestScheduler = requestScheduler;
        this.parserScheduler = parserScheduler;
        this.website = "";
    }

    public void run(){
        //System.out.println(Thread.currentThread().getName() + ":begin to :" + website);
        //String content = getPage(website);

        while(true) {
            website = requestScheduler.pop();
            if(website != null) {
                parserScheduler.push(getPage(website));//将抓取到的页面内容push到解析页面中
                System.out.println(Thread.currentThread().getName() + ":push page:" + website + " to parser queue.");
            }else{
                try {
                    System.out.println(Thread.currentThread().getName() + ":sleep for 100ms for the website is null.");
                    Thread.sleep(100);

                }catch (InterruptedException ie){
                    ie.printStackTrace();
                }
            }
        }
    }

    //获取指定URL的页面内容  页面类  数据读取
    public static Page getPage(String website){
        String content = "";
        BufferedReader read = null;
        Page page = new Page();
        if(website == null){
            return page;
        }
        try {
            System.out.println(Thread.currentThread().getName() + ":begin to download website:" + website);
            URL url = new URL(website);
            URLConnection uc = url.openConnection();
            uc.connect();
            read = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String tmp = "";
            while((tmp = read.readLine()) != null){
                //System.out.println("test");
                content = content + tmp;
            }
        }catch (MalformedURLException mue){
            mue.getMessage();
        }catch (IOException ie){
            ie.getMessage();
        }finally{
            try {
                if (read != null) {
                    read.close();
                }
            }catch(IOException ie){
                ie.getMessage();
            }
        }
        //System.out.println("content:");
        //System.out.println(content);
        System.out.println(Thread.currentThread().getName() + ":complete download the website:" + website + ":" +content);
        //System.out.println("website:" + website);
        page.setWebsite(website);
        page.setPage(content);

        return page;
    }
}
