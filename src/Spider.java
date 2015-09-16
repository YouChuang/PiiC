import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LaoJia on 2015/9/9.
 * 抽象出来类、功能模块（预处理、爬取内容、处理数据、存储数据、展示数据等等）   问题、答案、用户、爬虫(爬数据、解析数据、存储数据)
 * 数据处理方式：生产者-消费者模式(阻塞队列)  爬数据线程、分析数据线程、存储数据线程？
 * 加入多线程：数据访问并发处理
 * 数据存储：MySQL、Redis
 * 缓存优化、分布式优化等等
 */

/**
 * 抓取到每个问题对应的回答    问题、答案、用户，特定用户的回答/特定回答，特定问题的回答/特定回答，特定的回答
 * 数据已经抓取完毕，不过数据需要清洗，然后抓取的过程很慢，经常卡死停止   数据结构设计基本没问题了
 * 存储到文件
 * 目前该做：
 * MySQL或者Redis中
 * 多线程抓取数据、处理、存储数据以及对应的数据访问多线程修改
 * 数据类分层优化
 *
 */
public class Spider {

    public static void main(String[] args){
        String website = "http://www.zhihu.com/explore/recommendations";
        //String page = getPage(website);
        //System.out.println(page);
        //()表示分组方便后面只提取出来URL地址  question_link.+?>(.+?)<
       // String pattern = "question_link.+?href=\"(.+?)\">(.+?)<";//"question_link.+?href=\"(.+?)\"";//"src=\"(.+?)\"";question_link.+?href=\"(.+?)\"
        List<Question> list = regexPageForQuestion(website);
        //String patResult[] = regexPage(pattern, page);
        //List<Question> list = regexPage(pattern, page);
        String data = null;
        data = printAllQuestions(list);
        data = trimData(data);
        saveDataToFile(data, "E:/Zhihu/Recommods.txt");
        //System.out.println(list.get(0).getQuestion() + "=" +list.get(0).getUrl());

    }
    //遍历已经抓取到的问题及其链接地址
    public static String printAllQuestions(List<Question> list){
        String data = new String("");
        if(list.size() < 1)
            return data;
        for(int i = 0; i < list.size(); i++){
            System.out.println("QuestionId:" + list.get(i).getQuestionid());
            data = data + "QuestionId:" + list.get(i).getQuestionid() + "\r\n";

            System.out.println("Question:" + list.get(i).getQuestion());
            data = data + "Question:" + list.get(i).getQuestion() + "\r\n";

            System.out.println("Number of Answers:" + list.get(i).getAnswers().size());
            data = data + "Number of Answers:" + list.get(i).getAnswers().size() + "\r\n";
            System.out.println("");
            for(int j = 0; j < list.get(i).getAnswers().size(); j++){
                System.out.println("AnswerId:" + list.get(i).getAnswers().get(j).getAnswerid());
                data = data + "AnswerId:" + list.get(i).getAnswers().get(j).getAnswerid() + "\r\n";
                System.out.println("UserId:" + list.get(i).getAnswers().get(j).getUserid());
                data = data + "UserId:" + list.get(i).getAnswers().get(j).getUserid() + "\r\n";
                System.out.println("Vote:" + list.get(i).getAnswers().get(j).getVote());
                data = data + "Vote:" + list.get(i).getAnswers().get(j).getVote() + "\r\n";
                System.out.println("Content:" + list.get(i).getAnswers().get(j).getContent());
                data = data + "Content:" + list.get(i).getAnswers().get(j).getContent() + "\r\n";
                data = data + "\r\n\r\n\r\n";
            }
            data = data + "\r\n\r\n\r\n\r\n\n\n";
            System.out.printf("\n\n\n\n\n");
        }
        return data;
    }

    //获取指定URL的页面内容  页面类  数据读取
    public static String getPage(String website){
        String content = "";
        BufferedReader read = null;
        try {
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
        }
        finally{
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
        return content;
    }

    //解析页面获取到问题和链接
    public static List<Question> regexPageForQuestion(String website){
        String page = getPage(website);//获取到页面内容
        //String pattern = "question_link.+?href=\"(.+?)\">(.+?)<";//String pattern = "question_link.+?href=\"(.+?)\">(.+?)<";
        String pattern = "<h2>.+?question_link.+?href=\"(.+?)\".+?</h2>";//"<h2>.+?question_link.+?href=\"(.+?)\".+?</h2>"
        //开始解析
        System.out.println("begin to regex.");
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(page);
        List<Question> list = new ArrayList<Question>();
        String questionUrl  = "";

        while (matcher.find()){//尝试查找与模式匹配的字符序列的下一个子序列
        //if (matcher.find()){
            //group(0)为整个表达式，剩下的有几个括号就有几个分组，括号可以嵌套

            questionUrl = regexForQuestionUrl(matcher.group(1));
            //拿到问题的URL后开始获取对应的问题具体信息以及回答的内容
            System.out.println("start to regix the url:" + questionUrl);
            Question qt = regexPageForAnswers(questionUrl);
            //Question qt = new Question(questionUrl, null, null, null);
            System.out.println("complete to regix the url:" + questionUrl);
            list.add(qt);
            //System.out.println("group0=" + matcher.group(0) + "  group1=" + matcher.group(1) + " group2=" + matcher.group(2));
        }
        return list;
    }
    //处理获取到的URL，解析出来问题的URL
    public static String regexForQuestionUrl(String url){
        //http://www.zhihu.com/question/21215630/answer/35680980  转换成 http://www.zhihu.com/question/21215630/
        String pattern = "question/(.*?)/";
        String questionUrl = "";
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(url);
        if(matcher.find()){
            questionUrl = "http://www.zhihu.com/question/" + matcher.group(1);
            //System.out.println("questionUrl=" + questionUrl);
            return questionUrl;
        }
        //System.out.println("UrlquestionUrl=" + url);
        return url;
    }

    //解析页面获取到问题对应的答案
    public static Question regexPageForAnswers(String questionId){
        String page = getPage(questionId);//获取到页面内容
        //String pattern = "question_link.+?href=\"(.+?)\">(.+?)<";//String pattern = "question_link.+?href=\"(.+?)\">(.+?)<";
        Question qt = new Question(questionId);

        Pattern pat = null;
        Matcher matcher = null;
        //先解析出来问题
        String title = "zh-question-title.+?<h2.+?>(.+?)</h2>";//"<h2>.+?question_link.+?href=\"(.+?)\".+?</h2>"
        pat = Pattern.compile(title);
        matcher = pat.matcher(page);
        if(matcher.find()){
            System.out.println("regix the content of the question:" + matcher.group(1));
            qt.setQuestion(matcher.group(1));
        }
        //问题描述
        String desc = "zh-question-detail.+?<div.+?>(.+?)</div>";//zh-question-detail.+?<div.+?>(.*?)</div>
        pat = Pattern.compile(desc);
        matcher = pat.matcher(page);
        if(matcher.find()){
            System.out.println("regix the description of question:" + matcher.group(1));
            qt.setDescription(matcher.group(1));
        }

        //解析答案
        //先解析答案的个数  zh-answers-filter  若是不为0，则继续解析
        String answerNum = "data-num=\"(.+?)\".+?zh-question-answer-num";//zh-question-answer-num   id=\"zh-question-answer-num\"
        //zm-item-answer   zm-item-answer-author-info  <a data-tip="p$t$feifeimao" href="/people/feifeimao">肥肥猫</a>  zm-item-vote-info
        //zm-item-rich-text    answer-date-link last_updated meta-item
        pat = Pattern.compile(answerNum);
        matcher = pat.matcher(page);
        if(matcher.find()){
            int num = Integer.valueOf(matcher.group(1));
            if(num > 0){//回答的个数不为0，则继续爬取各个答案
                //获取作者ID 暂时不需要名字  需要的话去作者主页去抓   一次抓取一个回答相关的作者ID、赞同数量和回答内容  href="/question/35225468/answer/62382322"
                String answer = "zm-item-answer.+?class=\"count\">(.+?)<.+?class=\"zm-item-link-avatar\".+?href=\"(.+?)\">.+?/answer/content.+?<div.+?>(.+?)</div>.+?answer-date-link.+?href=\"(.+?)\"";
                pat = Pattern.compile(answer);
                matcher = pat.matcher(page);
                Answer ans = null;
                while(matcher.find()){
                    //System.out.println("vote:" + matcher.group(1));
                    //System.out.println("user:" + matcher.group(2));//匿名用户的ID是href="javascript:void(0);"
                    //System.out.println("answer:" + matcher.group(3));
                    //System.out.println("answerid:" + matcher.group(4));
                    //一个答案
                    System.out.println("regix the answerid of question:" + matcher.group(4));
                    ans = new Answer();
                    ans.setAnswerid(matcher.group(4));
                    //ans.setQuestionid();
                    ans.setUserid(matcher.group(2));
                    ans.setContent(matcher.group(3));
                    ans.setVote(Integer.valueOf(matcher.group(1)));
                    qt.getAnswers().add(ans);

                }
            }else {
                System.out.println("question " + questionId + " no answers.");
            }
        }else {
            System.out.println("data-num null" );
        }
        return qt;
    }

    //暂时先采用list来存放Question对象
    public static List<Question> regexPage(String pattern, String page){
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(page);
        //Map map = new HashMap();
        List<Question> list = new ArrayList<Question>();
        while (matcher.find()){//尝试查找与模式匹配的字符序列的下一个子序列
            //group(0)为整个表达式，剩下的有几个括号就有几个分组，括号可以嵌套
            Question qt = new Question("http://www.zhihu.com" + matcher.group(1), matcher.group(2), null, null);
            list.add(qt);
            //System.out.println("group0=" + matcher.group(0) + "  group1=" + matcher.group(1) + " group2=" + matcher.group(2));
        }
        return list;
    }
    //存储解析出来的数据   存储类  存放解析结果   数据、文件路径
    public static boolean saveDataToFile(String data, String path){
        System.out.println("start to write data...");
        if(path == null || data == null)
            return false;
        boolean ret = true;

        //创建目录
        int lastIndex = path.lastIndexOf("/");//找到路径中最后一个/
        if(lastIndex == 0)
            return false;

        String dir = path.substring(0, lastIndex);//截取目录地址
        System.out.println("create mkdir:" + dir);
        File fileDir = new File(dir);
        if(fileDir.mkdirs() == false){
            System.out.println("create mkdir error.");
            ret = false;
        }
        //创建存放数据的文件
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
            System.out.println("Write Success...");
        }else{
            System.out.println("Write Error...");
        }
        return ret;
    }
    //格式整理
    public static String trimData(String data){
        data = data.replace("<br>", "\r\n");
        //data = data.
        return data;
    }
}
