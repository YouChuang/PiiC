package core.persistor;

import core.Outcome;
import core.scheduler.PersistenceScheduler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LaoJia on 2015/9/12.
 */
/*
文件持久化类
将抓取的数据持久化到文件中存放
 */
public class FilePersistor implements Runnable{

    private PersistenceScheduler persistenceScheduler;
    private Outcome out;


    public FilePersistor(PersistenceScheduler persistenceScheduler){
        //this.persistenceScheduler = persistenceScheduler;
        System.out.println(Thread.currentThread().getName() + ":FilePersistor is initialized.");
        this.persistenceScheduler = persistenceScheduler;
        out = null;
    }

    public void run(){
        while(true) {
            System.out.println(Thread.currentThread().getName() + ":FilePersistor try to pop...");
            out = persistenceScheduler.pop();
            if(out != null) {
                //String path;//将问题的URL地址当做文件的名称  http.www.zhihu.com.question.27621722   http://www.zhihu.com/question/27621722
                saveDataToFile(out.getContent(), regexUrlForFilename(out.getWebsite()));
                System.out.println("Time to save the data:" + new Date().toString());
                //saveDataToFile(outMap.get())
            }else{
                try{
                    System.out.println(Thread.currentThread().getName() + ":sleep for 100ms for the outcome is null.");
                    Thread.sleep(100);
                }catch (InterruptedException ie){
                    ie.printStackTrace();
                }
            }
        }
    }

    public static String regexUrlForFilename(String website){
        System.out.println(Thread.currentThread().getName() + ":original website:" + website);//http://www.zhihu.com/question/27621722
        String path = website.substring(21);
        path = path.replace("/", ".");
        return path;
        /*
        Pattern pat = null;
        Matcher matcher = null;
        //先解析出来问题



        String site = "com/(.+?)";//"<h2>.+?question_link.+?href=\"(.+?)\".+?</h2>"
        pat = Pattern.compile(site);
        matcher = pat.matcher(website);
        if(matcher.find()){
            path = matcher.group(1);
            path = path.replace("/", ".");
        }
        System.out.println(Thread.currentThread().getName() + ":file name:" + path);

        return path;
        */
    }

    //存储解析出来的数据   存储类  存放解析结果   数据、文件路径
    public static boolean saveDataToFile(String data, String path){
        System.out.println(Thread.currentThread().getName() + ":start to create file:" + path);
        if(path == null || data == null)
            return false;
        boolean ret = true;

        /*
        //创建目录
        int lastIndex = path.lastIndexOf("/");//找到路径中最后一个/
        if(lastIndex == 0)
            return false;

        String dir = path.substring(0, lastIndex);//截取目录地址
        System.out.println(Thread.currentThread().getName() + ":create mkdir:" + dir);
        */
        String dir = "E:/Zhihu";//目录先这么固定
        File fileDir = new File(dir);
        if(fileDir.mkdirs() == false){
            System.out.println(Thread.currentThread().getName() + ":create mkdir error.");
            ret = false;
        }
        //创建存放数据的文件
        path = dir + "/" + path + ".txt";
        System.out.println(Thread.currentThread().getName() + ":final path:" + path);
        File file = new File(path);
        FileWriter write = null;
        //写入文件
        try {
            write = new FileWriter(file);
            write.write(data);
        }catch(IOException ie){
            ie.printStackTrace();
            ret = false;
        }finally{
            try {
                if (write != null) {
                    write.close();
                }
            }catch(IOException ie){
                ie.printStackTrace();
                ret = false;
            }
        }
        if(ret) {
            System.out.println(Thread.currentThread().getName() + ":Write Success...");
        }else{
            System.out.println(Thread.currentThread().getName() + ":Write Error...");
        }
        return ret;
    }
}
