package core;

/**
 * Created by LaoJia on 2015/9/15.
 */
/*
��ҳ��Ϣ��
URL��ַ
ҳ������
 */
public class Page {
    private String website;
    private String page;
    public Page(){
        this.website = null;
        this.page = null;
    }
    public Page(String website, String page){
        this.website = website;
        this.page = page;
    }

    public void setPage(String page){
        this.page = page;
    }
    public String getPage(){
        return page;
    }

    public void setWebsite(String website){
        this.website = website;
    }
    public String getWebsite(){
        return website;
    }
}
