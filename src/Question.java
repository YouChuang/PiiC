import java.util.ArrayList;

/**
 * Created by LaoJia on 2015/9/9.
 * 知乎的问题
 */
public class Question {
    private String questionid;//唯一的链接
    private String question;//问题内容
    private String description;//问题描述
    private ArrayList<Answer> answers;//问题和回答是一对多的关系  这个以后要考虑并发的问题
    //Answer

    //构造函数   怎么使用单例模式
    public Question(String questionid){
        this.questionid = questionid;
        this.question = null;
        this.description = null;
        this.answers = new ArrayList<Answer>();//不能赋值为null
    }
    public Question(String questionid, String question, String description, ArrayList<Answer> answers){
        this.questionid = questionid;
        this.description = description;
        this.question = question;
        this.answers = answers;//答案初始时可能为空
    }
    public String getQuestion(){
        return question;
    }
    public void setQuestion(String question){
        this.question = question;
    }
    public String getQuestionid(){
        return questionid;
    }
    public void setQuestionid(String questionid){
        this.questionid = questionid;
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public ArrayList<Answer> getAnswers(){
        return answers;
    }
    public void setAnswers(ArrayList<Answer> answers){
        this.answers = answers;
    }

    //解析问题对应的标题、描述以及答案等信息
    public void regexQuestion(String questionid){
        //抓取问题页面的内容
        //解析页面内容
        //获取标题、描述、答案
    }


}
