import java.util.ArrayList;

/**
 * Created by LaoJia on 2015/9/9.
 * �û���
 */
public class User {
    private String userid;
    private String username;//�û���
    private ArrayList<Answer> answers;//һ���û���Ӧ�Ļش�

    public User(String userid, String username, ArrayList<Answer> answers){
        this.userid = userid;
        this.username = username;
        this.answers = answers;
    }
    public String getUserid(){
        return userid;
    }
    public String getUsername(){
        return username;
    }
    public ArrayList<Answer> getAnswers(){
        return answers;
    }
}
