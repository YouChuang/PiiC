package core;

import java.util.Map;

/**
 * Created by LaoJia on 2015/9/15.
 */
/*
��ȡ�Ľ����
��Ӧ��URL
��ȥ�����ݼ�  ����K-V���洢������    ҳ���ַ-������ҳ������
Ŀǰ�洢����֪�������ַ-������ҳ�����ݣ��𰸵ȣ�
��չ��  ΢�����û���ַ-������ҳ�����ݣ�΢����������΢����ַ-������ҳ�����ݣ�΢����


���գ�ҳ���ַ-ҳ������ݣ�����ֻ�Ǵ洢�Ľ�����Ľ��������ǰ��û�д洢


 */
public class Outcome {
    private String website;
    private String content;

    public Outcome(){
        this.website = null;
        this.content = null;
    }

    public Outcome(String website, String content){
        this.website = website;
        this.content = content;
    }

    public String getWebsite(){
        return website;
    }
    public void setWebsite(String website){
        this.website = website;
    }
    public String getContent(){
        return content;
    }
    public void setContent(String content){
        this.content = content;
    }

}
