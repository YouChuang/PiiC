package core.scheduler;

/**
 * Created by LaoJia on 2015/9/13.
 */
/*
调度器接口
所谓的泛型接口
*/
public interface Scheduler<T> {
    public void push(T request);
    public T pop();
}
