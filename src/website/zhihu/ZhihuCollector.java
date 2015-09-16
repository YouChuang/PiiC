package website.zhihu;
import core.Spider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by LaoJia on 2015/9/14.
 */
public class ZhihuCollector {
    private String website;
    private List<String> sitelist;
    public ZhihuCollector(){
        sitelist = new ArrayList<String>();
        //this.website = website;
    }
    public String getUrl(){
        return website;
    }
    public static void main(String[] args){

        ZhihuCollector zhihu = new ZhihuCollector();
        zhihu.sitelist.add("http://www.zhihu.com/explore/recommendations");
        System.out.println("Time to begin to regex:" + new Date().toString());
        //zhihu.sitelist.add("http://www.zhihu.com/people/feifeimao");
        Spider spider = new Spider(zhihu.sitelist);
        spider.run();
    }
}
