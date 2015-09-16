import java.util.ArrayList;

/**
 * Created by LaoJia on 2015/9/9.
 * 用户类
 */
public class User {
    private String userid;
    private String username;//用户名
    private ArrayList<Answer> answers;//一个用户对应的回答

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
