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
�ļ��־û���
��ץȡ�����ݳ־û����ļ��д��
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
                //String path;//�������URL��ַ�����ļ�������  http.www.zhihu.com.question.27621722   http://www.zhihu.com/question/27621722
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
        //�Ƚ�����������



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

    //�洢��������������   �洢��  ��Ž������   ���ݡ��ļ�·��
    public static boolean saveDataToFile(String data, String path){
        System.out.println(Thread.currentThread().getName() + ":start to create file:" + path);
        if(path == null || data == null)
            return false;
        boolean ret = true;

        /*
        //����Ŀ¼
        int lastIndex = path.lastIndexOf("/");//�ҵ�·�������һ��/
        if(lastIndex == 0)
            return false;

        String dir = path.substring(0, lastIndex);//��ȡĿ¼��ַ
        System.out.println(Thread.currentThread().getName() + ":create mkdir:" + dir);
        */
        String dir = "E:/Zhihu";//Ŀ¼����ô�̶�
        File fileDir = new File(dir);
        if(fileDir.mkdirs() == false){
            System.out.println(Thread.currentThread().getName() + ":create mkdir error.");
            ret = false;
        }
        //����������ݵ��ļ�
        path = dir + "/" + path + ".txt";
        System.out.println(Thread.currentThread().getName() + ":final path:" + path);
        File file = new File(path);
        FileWriter write = null;
        //д���ļ�
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
