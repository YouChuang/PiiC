package core.scheduler;

/**
 * Created by LaoJia on 2015/9/13.
 */
/*
�������ӿ�
��ν�ķ��ͽӿ�
*/
public interface Scheduler<T> {
    public void push(T request);
    public T pop();
}
