import java.util.ArrayList;

/**
 * Created by LaoJia on 2015/9/9.
 * ֪��������
 */
public class Question {
    private String questionid;//Ψһ������
    private String question;//��������
    private String description;//��������
    private ArrayList<Answer> answers;//����ͻش���һ�Զ�Ĺ�ϵ  ����Ժ�Ҫ���ǲ���������
    //Answer

    //���캯��   ��ôʹ�õ���ģʽ
    public Question(String questionid){
        this.questionid = questionid;
        this.question = null;
        this.description = null;
        this.answers = new ArrayList<Answer>();//���ܸ�ֵΪnull
    }
    public Question(String questionid, String question, String description, ArrayList<Answer> answers){
        this.questionid = questionid;
        this.description = description;
        this.question = question;
        this.answers = answers;//�𰸳�ʼʱ����Ϊ��
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

    //���������Ӧ�ı��⡢�����Լ��𰸵���Ϣ
    public void regexQuestion(String questionid){
        //ץȡ����ҳ�������
        //����ҳ������
        //��ȡ���⡢��������
    }


}
