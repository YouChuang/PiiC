package core;

import java.util.Map;

/**
 * Created by LaoJia on 2015/9/15.
 */
/*
爬取的结果类
对应的URL
爬去的数据集  都用K-V来存储合适吗    页面地址-整理后的页面内容
目前存储的是知乎问题地址-整理后的页面内容（答案等）
扩展：  微博，用户地址-整理后的页面内容（微博），热门微博地址-整理后的页面内容（微博）


最终：页面地址-页面的内容，不过只是存储的解析后的结果，解析前的没有存储


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
