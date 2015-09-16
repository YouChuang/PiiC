package core.parser;

import core.Outcome;
import core.Page;
import core.scheduler.ParserScheduler;
import core.scheduler.PersistenceScheduler;
import core.scheduler.RequestScheduler;
import website.zhihu.Answer;
import website.zhihu.Question;
import website.zhihu.ZhihuCollector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LaoJia on 2015/9/13.
 */

/*
实现基本的Parser接口
针对不同的站点定义不同的解析函数

问题列表页面
问题页面
人物页面

 */
public class ZhihuParser implements Parser, Runnable{
    private RequestScheduler requestScheduler;
    private ParserScheduler parserScheduler;
    private static PersistenceScheduler persistenceScheduler;
    private Page page;
    public ZhihuParser(RequestScheduler requestScheduler, ParserScheduler parserScheduler, PersistenceScheduler persistenceScheduler){
        this.requestScheduler = requestScheduler;
        this.parserScheduler = parserScheduler;
        this.persistenceScheduler = persistenceScheduler;
        this.page = null;
    }
    public void run(){
        //System.out.println(Thread.currentThread().getName() + ":regex zhihu page:" + page.getWebsite());
        while(true){
            page = parserScheduler.pop();
            if(page != null) {
                if (page.getWebsite().indexOf("question") >= 0) {//URL为答案链接  抓取数据  并加入持久化仓库
                    System.out.println(Thread.currentThread().getName() + ":regex zhihu question for answers:" + page.getWebsite());
                    regexQuestionForAnswers(page);

                } else if (page.getWebsite().indexOf("people") >= 0) {//URL为用户链接

                } else if (page.getWebsite().indexOf("answer") >= 0) {//URL为问题链接

                } else {//综合页面
                    System.out.println(Thread.currentThread().getName() + ":regex common page for questions:" + page.getWebsite());
                    regexPageForQuestions(page.getPage());//解析问题链接
                    System.out.println(Thread.currentThread().getName() + ":regex common page for questions:" + page.getWebsite() + " over.");
                }
            }else{
                try{
                    System.out.println(Thread.currentThread().getName() + ":sleep for 100ms for the page is null.");
                    Thread.sleep(100);

                }catch (InterruptedException ie){
                    ie.printStackTrace();
                }
            }
        }


        //System.out.println(Thread.currentThread().getName() + ":cannot regex the type of website:" + page.getWebsite());

    }

    public void regexPageForQuestions(String page){
        String pattern = "<h2>.+?question_link.+?href=\"(.+?)\".+?</h2>";
        System.out.println(Thread.currentThread().getName() + ":begin to regex.");
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(page);
        List<Question> list = new ArrayList<Question>();
        String questionUrl  = "";

        while (matcher.find()){//尝试查找与模式匹配的字符序列的下一个子序列
            //group(0)为整个表达式，剩下的有几个括号就有几个分组，括号可以嵌套

            questionUrl = regexUrlForQuestionUrl(matcher.group(1));
            //拿到问题的URL后开始获取对应的问题具体信息以及回答的内容
            System.out.println(Thread.currentThread().getName() + ":get the question url:" + questionUrl);
            requestScheduler.push(questionUrl);//将解析出来的问题链接push到请求队列中
            //Question qt = regexQuestionForAnswers(questionUrl);
        }
    }
    //处理获取到的URL，解析出来问题的URL
    public static String regexUrlForQuestionUrl(String url){
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
    public static Question regexQuestionForAnswers(Page page){
        //String page = getPage(questionId);//获取到页面内容
        //String pattern = "question_link.+?href=\"(.+?)\">(.+?)<";//String pattern = "question_link.+?href=\"(.+?)\">(.+?)<";
        String wholeQuestion = "";
        Question qt = new Question(page.getWebsite());
        Outcome out = new Outcome();
        out.setWebsite(page.getWebsite());

        Pattern pat = null;
        Matcher matcher = null;
        //先解析出来问题
        String title = "zh-question-title.+?<h2.+?>(.+?)</h2>";//"<h2>.+?question_link.+?href=\"(.+?)\".+?</h2>"
        pat = Pattern.compile(title);
        matcher = pat.matcher(page.getPage());
        if(matcher.find()){
            System.out.println(Thread.currentThread().getName() + ":regix the content of the question:" + matcher.group(1));
            wholeQuestion = wholeQuestion + "Question:" + matcher.group(1) + "\r\n";
            qt.setQuestion(matcher.group(1));
        }
        //问题描述
        String desc = "zh-question-detail.+?<div.+?>(.+?)</div>";//zh-question-detail.+?<div.+?>(.*?)</div>
        pat = Pattern.compile(desc);
        matcher = pat.matcher(page.getPage());
        if(matcher.find()){
            System.out.println(Thread.currentThread().getName() + ":regix the description of question:" + matcher.group(1));
            wholeQuestion = wholeQuestion + "Description:" + matcher.group(1) + "\r\n";
            qt.setDescription(matcher.group(1));
        }

        //解析答案
        //先解析答案的个数  zh-answers-filter  若是不为0，则继续解析
        String answerNum = "data-num=\"(.+?)\".+?zh-question-answer-num";//zh-question-answer-num   id=\"zh-question-answer-num\"
        //zm-item-answer   zm-item-answer-author-info  <a data-tip="p$t$feifeimao" href="/people/feifeimao">肥肥猫</a>  zm-item-vote-info
        //zm-item-rich-text    answer-date-link last_updated meta-item
        pat = Pattern.compile(answerNum);
        matcher = pat.matcher(page.getPage());
        if(matcher.find()){
            int num = Integer.valueOf(matcher.group(1));
            wholeQuestion = wholeQuestion + "Number of answers:" + matcher.group(1) + "\r\n";
            if(num > 0){//回答的个数不为0，则继续爬取各个答案
                //获取作者ID 暂时不需要名字  需要的话去作者主页去抓   一次抓取一个回答相关的作者ID、赞同数量和回答内容  href="/question/35225468/answer/62382322"
                String answer = "zm-item-answer.+?class=\"count\">(.+?)<.+?class=\"zm-item-link-avatar\".+?href=\"(.+?)\">.+?/answer/content.+?<div.+?>(.+?)</div>.+?answer-date-link.+?href=\"(.+?)\"";
                pat = Pattern.compile(answer);
                matcher = pat.matcher(page.getPage());
                Answer ans = null;
                while(matcher.find()){
                    //System.out.println("vote:" + matcher.group(1));
                    //System.out.println("user:" + matcher.group(2));//匿名用户的ID是href="javascript:void(0);"
                    //System.out.println("answer:" + matcher.group(3));
                    //System.out.println("answerid:" + matcher.group(4));
                    //一个答案
                    System.out.println(Thread.currentThread().getName() + ":regix the answerid of question:" + matcher.group(4));
                    wholeQuestion = wholeQuestion + "\r\nAnswerID:" + matcher.group(4) + "\r\n";
                    ans = new Answer();
                    ans.setAnswerid(matcher.group(4));
                    //ans.setQuestionid();
                    wholeQuestion = wholeQuestion + "UserID:" + matcher.group(2) + "\r\n";
                    ans.setUserid(matcher.group(2));
                    wholeQuestion = wholeQuestion + "Vote:" + matcher.group(1) + "\r\n";
                    //ans.setVote(Integer.valueOf(matcher.group(1)));
                    wholeQuestion = wholeQuestion + "Content:" + matcher.group(3) + "\r\n";
                    ans.setContent(matcher.group(3));

                    qt.getAnswers().add(ans);

                }
            }else {
                wholeQuestion = wholeQuestion + "Question: no answers \r\n";
                System.out.println(Thread.currentThread().getName() + ":question " + page.getWebsite() + " no answers.");
            }
        }else {
            System.out.println("data-num null" );
        }
        out.setContent(wholeQuestion);
        persistenceScheduler.push(out);

        return qt;
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

            questionUrl = regexUrlForQuestionUrl(matcher.group(1));
            //拿到问题的URL后开始获取对应的问题具体信息以及回答的内容
            System.out.println("start to regix the url:" + questionUrl);




            //Question qt = regexPageForAnswers(questionUrl);
            //website.zhihu.Question qt = new website.zhihu.Question(questionUrl, null, null, null);
            //System.out.println("complete to regix the url:" + questionUrl);
            //list.add(qt);
            //System.out.println("group0=" + matcher.group(0) + "  group1=" + matcher.group(1) + " group2=" + matcher.group(2));
        }
        return list;
    }



    public static String regexUrlForPage(String website){
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



    //public void regexQuestionForAnswers(String website){}
    public void regexPeopleForInfo(String website){}


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



}
