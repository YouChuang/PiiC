package website.zhihu;

/**
 * Created by LaoJia on 2015/9/9.
 * ��   ����������ھ���������
 */
public class Answer {
    private String answerid;//�û����ӣ�Ψһ
    private String questionid;//ֻ��Ӧһ�������Ѿ����ڵ���������ӱ��
    private String userid;//�û���
    private String content;//�ش�����  �����ʲô�洢�ȽϺ��ʣ��Ƚϳ��Ļش𣬻���ͼƬ  ��ô����������ʾ
    private int vote;//��ͬ����

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
