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
 * ��������ࡢ����ģ�飨Ԥ������ȡ���ݡ��������ݡ��洢���ݡ�չʾ���ݵȵȣ�   ���⡢�𰸡��û�������(�����ݡ��������ݡ��洢����)
 * ���ݴ���ʽ��������-������ģʽ(��������)  �������̡߳����������̡߳��洢�����̣߳�
 * ������̣߳����ݷ��ʲ�������
 * ���ݴ洢��MySQL��Redis
 * �����Ż����ֲ�ʽ�Ż��ȵ�
 */

/**
 * ץȡ��ÿ�������Ӧ�Ļش�    ���⡢�𰸡��û����ض��û��Ļش�/�ض��ش��ض�����Ļش�/�ض��ش��ض��Ļش�
 * �����Ѿ�ץȡ��ϣ�����������Ҫ��ϴ��Ȼ��ץȡ�Ĺ��̺�������������ֹͣ   ���ݽṹ��ƻ���û������
 * �洢���ļ�
 * Ŀǰ������
 * MySQL����Redis��
 * ���߳�ץȡ���ݡ������洢�����Լ���Ӧ�����ݷ��ʶ��߳��޸�
 * ������ֲ��Ż�
 *
 */
public class Spider {

    public static void main(String[] args){
        String website = "http://www.zhihu.com/explore/recommendations";
        //String page = getPage(website);
        //System.out.println(page);
        //()��ʾ���鷽�����ֻ��ȡ����URL��ַ  question_link.+?>(.+?)<
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
    //�����Ѿ�ץȡ�������⼰�����ӵ�ַ
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

    //��ȡָ��URL��ҳ������  ҳ����  ���ݶ�ȡ
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

    //����ҳ���ȡ�����������
    public static List<Question> regexPageForQuestion(String website){
        String page = getPage(website);//��ȡ��ҳ������
        //String pattern = "question_link.+?href=\"(.+?)\">(.+?)<";//String pattern = "question_link.+?href=\"(.+?)\">(.+?)<";
        String pattern = "<h2>.+?question_link.+?href=\"(.+?)\".+?</h2>";//"<h2>.+?question_link.+?href=\"(.+?)\".+?</h2>"
        //��ʼ����
        System.out.println("begin to regex.");
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(page);
        List<Question> list = new ArrayList<Question>();
        String questionUrl  = "";

        while (matcher.find()){//���Բ�����ģʽƥ����ַ����е���һ��������
        //if (matcher.find()){
            //group(0)Ϊ�������ʽ��ʣ�µ��м������ž��м������飬���ſ���Ƕ��

            questionUrl = regexForQuestionUrl(matcher.group(1));
            //�õ������URL��ʼ��ȡ��Ӧ�����������Ϣ�Լ��ش������
            System.out.println("start to regix the url:" + questionUrl);
            Question qt = regexPageForAnswers(questionUrl);
            //Question qt = new Question(questionUrl, null, null, null);
            System.out.println("complete to regix the url:" + questionUrl);
            list.add(qt);
            //System.out.println("group0=" + matcher.group(0) + "  group1=" + matcher.group(1) + " group2=" + matcher.group(2));
        }
        return list;
    }
    //�����ȡ����URL���������������URL
    public static String regexForQuestionUrl(String url){
        //http://www.zhihu.com/question/21215630/answer/35680980  ת���� http://www.zhihu.com/question/21215630/
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

    //����ҳ���ȡ�������Ӧ�Ĵ�
    public static Question regexPageForAnswers(String questionId){
        String page = getPage(questionId);//��ȡ��ҳ������
        //String pattern = "question_link.+?href=\"(.+?)\">(.+?)<";//String pattern = "question_link.+?href=\"(.+?)\">(.+?)<";
        Question qt = new Question(questionId);

        Pattern pat = null;
        Matcher matcher = null;
        //�Ƚ�����������
        String title = "zh-question-title.+?<h2.+?>(.+?)</h2>";//"<h2>.+?question_link.+?href=\"(.+?)\".+?</h2>"
        pat = Pattern.compile(title);
        matcher = pat.matcher(page);
        if(matcher.find()){
            System.out.println("regix the content of the question:" + matcher.group(1));
            qt.setQuestion(matcher.group(1));
        }
        //��������
        String desc = "zh-question-detail.+?<div.+?>(.+?)</div>";//zh-question-detail.+?<div.+?>(.*?)</div>
        pat = Pattern.compile(desc);
        matcher = pat.matcher(page);
        if(matcher.find()){
            System.out.println("regix the description of question:" + matcher.group(1));
            qt.setDescription(matcher.group(1));
        }

        //������
        //�Ƚ����𰸵ĸ���  zh-answers-filter  ���ǲ�Ϊ0�����������
        String answerNum = "data-num=\"(.+?)\".+?zh-question-answer-num";//zh-question-answer-num   id=\"zh-question-answer-num\"
        //zm-item-answer   zm-item-answer-author-info  <a data-tip="p$t$feifeimao" href="/people/feifeimao">�ʷ�è</a>  zm-item-vote-info
        //zm-item-rich-text    answer-date-link last_updated meta-item
        pat = Pattern.compile(answerNum);
        matcher = pat.matcher(page);
        if(matcher.find()){
            int num = Integer.valueOf(matcher.group(1));
            if(num > 0){//�ش�ĸ�����Ϊ0���������ȡ������
                //��ȡ����ID ��ʱ����Ҫ����  ��Ҫ�Ļ�ȥ������ҳȥץ   һ��ץȡһ���ش���ص�����ID����ͬ�����ͻش�����  href="/question/35225468/answer/62382322"
                String answer = "zm-item-answer.+?class=\"count\">(.+?)<.+?class=\"zm-item-link-avatar\".+?href=\"(.+?)\">.+?/answer/content.+?<div.+?>(.+?)</div>.+?answer-date-link.+?href=\"(.+?)\"";
                pat = Pattern.compile(answer);
                matcher = pat.matcher(page);
                Answer ans = null;
                while(matcher.find()){
                    //System.out.println("vote:" + matcher.group(1));
                    //System.out.println("user:" + matcher.group(2));//�����û���ID��href="javascript:void(0);"
                    //System.out.println("answer:" + matcher.group(3));
                    //System.out.println("answerid:" + matcher.group(4));
                    //һ����
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

    //��ʱ�Ȳ���list�����Question����
    public static List<Question> regexPage(String pattern, String page){
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(page);
        //Map map = new HashMap();
        List<Question> list = new ArrayList<Question>();
        while (matcher.find()){//���Բ�����ģʽƥ����ַ����е���һ��������
            //group(0)Ϊ�������ʽ��ʣ�µ��м������ž��м������飬���ſ���Ƕ��
            Question qt = new Question("http://www.zhihu.com" + matcher.group(1), matcher.group(2), null, null);
            list.add(qt);
            //System.out.println("group0=" + matcher.group(0) + "  group1=" + matcher.group(1) + " group2=" + matcher.group(2));
        }
        return list;
    }
    //�洢��������������   �洢��  ��Ž������   ���ݡ��ļ�·��
    public static boolean saveDataToFile(String data, String path){
        System.out.println("start to write data...");
        if(path == null || data == null)
            return false;
        boolean ret = true;

        //����Ŀ¼
        int lastIndex = path.lastIndexOf("/");//�ҵ�·�������һ��/
        if(lastIndex == 0)
            return false;

        String dir = path.substring(0, lastIndex);//��ȡĿ¼��ַ
        System.out.println("create mkdir:" + dir);
        File fileDir = new File(dir);
        if(fileDir.mkdirs() == false){
            System.out.println("create mkdir error.");
            ret = false;
        }
        //����������ݵ��ļ�
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
            System.out.println("Write Success...");
        }else{
            System.out.println("Write Error...");
        }
        return ret;
    }
    //��ʽ����
    public static String trimData(String data){
        data = data.replace("<br>", "\r\n");
        //data = data.
        return data;
    }
}
