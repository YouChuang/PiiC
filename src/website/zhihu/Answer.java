package website.zhihu;

/**
 * Created by LaoJia on 2015/9/9.
 * 答案   这个是依赖于具体的问题的
 */
public class Answer {
    private String answerid;//用户链接，唯一
    private String questionid;//只对应一个现在已经存在的问题的链接编号
    private String userid;//用户名
    private String content;//回答内容  这个用什么存储比较合适？比较长的回答，还有图片  怎么解析出来显示
    private int vote;//赞同人数

    public Answer(){
        this.answerid = null;
        this.questionid = null;
        this.userid = null;
        this.content = null;
        this.vote = 0;
    }
    public Answer(String answerid, String questionid, String userid, String content, int vote){
        this.answerid = answerid;
        this.questionid = questionid;
        this.userid = userid;
        this.content = content;
        this.vote = vote;
    }
    public String getAnswerid(){
        return answerid;
    }
    public void setAnswerid(String answerid){
        this.answerid = answerid;
    }
    public String getQuestionid(){
        return questionid;
    }
    public void setQuestionid(String questionid){
        this.questionid = questionid;
    }
    public String getUserid(){
        return userid;
    }
    public void setUserid(String userid){
        this.userid = userid;
    }
    public String getContent(){
        return content;
    }
    public void setContent(String content){
        this.content = content;
    }
    public int getVote(){
        return vote;
    }
    public void setVote(int vote){
        this.vote = vote;
    }
}
