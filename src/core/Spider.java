package core;

import core.downloader.UrlDownloader;
import core.parser.ZhihuParser;
import core.persistor.FilePersistor;
import core.scheduler.ParserScheduler;
import core.scheduler.PersistenceScheduler;
import core.scheduler.RequestScheduler;
import website.zhihu.Answer;
import website.zhihu.Question;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
个性化网络信息收集器：
    Personalized Internet Information Collector  PIIC
    知乎：关注者的好回答、关注话题的好回答，新的有趣话题
    微博：关注着、关注话题，热门
    澎湃新闻：热门、关注话题

web页面
移动客户端

做东西的习惯：
    用户需求思维、创造价值思维（用户价值、经济价值）  非技术思维
    产品、服务的形式

*/

/*
架构和实现：
    抽象出来类、功能模块（预处理、爬取内容、处理数据、存储数据、展示数据等等）   问题、答案、用户、爬虫(爬数据、解析数据、存储数据)
    数据处理方式：生产者-消费者模式(阻塞队列)  爬数据线程、分析数据线程、存储数据线程？
    加入多线程：数据访问并发处理
    数据存储：MySQL、Redis
    缓存优化、分布式优化等等
    HttpClient抓取页面
    Jsoup解析页面
    spring+redis
    分布式抓取
    分布式存储
    Hadoop

大规模实际应用的问题：
    网页规则匹配问题：如何尽量减少对每个网站设定单独匹配规则的问题  手动分类，机器学习
    重复网页问题：URL重复，网页内容重复     布隆过滤器，相似度聚合，分类海明距离判断
        ###URL是否重复：布隆过滤器
        网页内容是否重复：
            一般方法：特征抽取，形成文档指纹，与已有文档的指纹进行相似性计算
            SimHash：特征抽取，
    ###多线程抓取 分布式抓取        Hadoop

    ###任务调度    redis Scheduler？
    ###大规模存储  分布式存储     hdfs
    反屏蔽

*/

/*
工作流程：
    用户发出URL爬取请求
    将请求加入用户请求调度器队列(URL请求(包括解析出来的URL)-下载器)
    线程从队列中取出请求，查询已爬取对列，若是不存在则将请求加入到已爬取队列，若是已存在则不处理 读写锁模式访问已爬取队列    生产者消费者模型读写请求队列
    请求交由下载器去抓取数据
    并将数据加入页面解析请求调度器队列(下载器-解析器)
    线程从队列中取出请求，并将解析请求记录为已解析处理
    请求交由解析器去解析有用的数据，并放入数据持久化队列(解析器-持久化器)
    将有用的数据持久化

    //初始化-URL请求仓库-下载器-页面仓库-解析器(-URL请求仓库-下载器)/(-内容仓库-持久化器)
    //解析器-URL请求仓库-下载器
    //解析器-页面仓库-存储器

    用户请求调度
    页面解析请求地调度
    数据持久化请求调度

    LinkedBlockingQueue  阻塞队列，实现生产者-消费者模型
    读写锁  实现对数据的共享读、独占写模型

*/


/**
 * 抓取到每个问题对应的回答    问题、答案、用户，特定用户的回答/特定回答，特定问题的回答/特定回答，特定的回答
 * 数据已经抓取完毕，不过数据需要清洗，然后抓取的过程很慢，经常卡死停止   数据结构设计基本没问题了
 * 存储到文件
 * 多线程抓取数据、处理、存储数据以及对应的数据访问多线程修改
 *
 * 目前该做：
 * URL重复检测
 *
 * MySQL或者Redis，数据类spring分层优化
 *
 * httpclient  jsoup
 *
 */
public class Spider implements Runnable{
    private List<String> sitelist;
    private String website;
    private RequestScheduler requestScheduler = RequestScheduler.getInstance();
    private ParserScheduler parserScheduler = ParserScheduler.getInstance();
    private PersistenceScheduler persistenceScheduler = PersistenceScheduler.getInstance();


    public Spider(List<String> list){
        //scheduler.push(website);
        //this.website = website;
        this.sitelist = list;
    }

    public static void main(String[] args){
        String website = "http://www.zhihu.com/explore/recommendations";
        //String page = getPage(website);
        //System.out.println(page);
        //()表示分组方便后面只提取出来URL地址  question_link.+?>(.+?)<
        // String pattern = "question_link.+?href=\"(.+?)\">(.+?)<";//"question_link.+?href=\"(.+?)\"";//"src=\"(.+?)\"";question_link.+?href=\"(.+?)\"

        //下载页面

        List<Question> list = regexPageForQuestion(website);
        //String patResult[] = regexPage(pattern, page);
        //List<website.zhihu.Question> list = regexPage(pattern, page);
        String data = null;
        data = printAllQuestions(list);
        data = trimData(data);

        //存储数据到文件
        //FilePersistor fp = new FilePersistor();
        //fp.saveDataToFile(data, "E:/zhihu/Recommods.txt");

        //System.out.println(list.get(0).getQuestion() + "=" +list.get(0).getUrl());

    }

    public void run(){
        //线程池  默认容量为10个
        ExecutorService executorService = Executors.newFixedThreadPool(12);
        //请求调度器
        //RequestScheduler request = RequestScheduler.getInstance();
        //创建任务线程并执行
        UrlDownloader downloader;
        ZhihuParser parser;
        FilePersistor persistor;
        String website = "";
        Page page = null;
        Outcome out = null;


        //初始化处理队列
        while(sitelist.size() > 0){
            requestScheduler.push(sitelist.get(0));
            sitelist.remove(0);
        }
        //
        //String website = requestScheduler.pop();
        for(int i = 0; i < 3; i++) {
            downloader = new UrlDownloader(requestScheduler, parserScheduler);
            executorService.execute(downloader);
        }
        //解析页面
        for(int i = 0; i < 3; i++) {
            parser = new ZhihuParser(requestScheduler, parserScheduler, persistenceScheduler);
            executorService.execute(parser);
        }
        //存储数据
        for(int i = 0; i < 3; i++) {
            persistor = new FilePersistor(persistenceScheduler);
            executorService.execute(persistor);
        }
        executorService.shutdown();
    }
    static class StopListener implements Runnable{
        public void run(){
            System.out.println("= means stop.");
            while(true){
                try {
                    int in = System.in.read();
                    if(in == '='){

                    }
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }
            }
        }
    }


    //遍历已经抓取到的问题及其链接地址
    public static String printAllQuestions(List<Question> list){
        String data = new String("");
        if(list.size() < 1)
            return data;
        for(int i = 0; i < list.size(); i++){
            System.out.println("QuestionId:" + list.get(i).getQuestionid());
            data = data + "QuestionId:" + list.get(i).getQuestionid() + "\r\n";

            System.out.println("website.zhihu.Question:" + list.get(i).getQuestion());
            data = data + "website.zhihu.Question:" + list.get(i).getQuestion() + "\r\n";

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
            //website.zhihu.Question qt = new website.zhihu.Question(questionUrl, null, null, null);
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

    //格式整理
    public static String trimData(String data){
        data = data.replace("<br>", "\r\n");
        //data = data.
        return data;
    }
}
