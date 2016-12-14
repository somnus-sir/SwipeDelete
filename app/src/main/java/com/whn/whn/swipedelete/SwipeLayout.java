package com.whn.whn.swipedelete;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import static android.content.ContentValues.TAG;

/**
 * Created by whn on 2016/12/13.
 */

public class SwipeLayout extends FrameLayout {

    private View delete;
    private View content;
    private ViewDragHelper viewDragHelper;
    private float downX;
    private float downY;
    private long mTime;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        viewDragHelper = ViewDragHelper.create(this, mCallBack);
    }

    /**
     * viewDragHelper帮助我们判断是否应该拦截
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = viewDragHelper.shouldInterceptTouchEvent(ev);
        return result;
    }

    /**
     * viewDragHelper获取触摸事件,判断移动,请求不拦截
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                mTime = System.currentTimeMillis();
                if(listener!=null){
                    listener.onTouchDown(SwipeLayout.this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();

                float dx = Math.abs(moveX-downX);
                float dy = Math.abs(moveY-downY);

                if(dx>dy){
                    //横向滑动请求父类不拦截
                    requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                //1.按下抬起的时间
                long duration = System.currentTimeMillis() - mTime;
                //2.计算按下抬起的距离
                float deltaX = event.getX() - downX;
                float deltaY = event.getY() - downY;
                float distance = (float) Math.sqrt(Math.pow(deltaX,2)+Math.pow(deltaY,2));
                //如果按下抬起的时间小于500，并且按下抬起的距离小于8像素
                if(duration< ViewConfiguration.getLongPressTimeout() && distance<
                        ViewConfiguration.getTouchSlop()){
                    //则认为是满足了执行点击的条件
                    performClick();//作用就是执行OnClickListener的onClick方法
                }
                break;
        }
        //获取触摸事件
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * 自定义排版,放好位置
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        content.layout(0, 0, content.getMeasuredWidth(), content.getMeasuredHeight());
        delete.layout(content.getMeasuredWidth(), 0, content.getMeasuredWidth() + delete.getMeasuredWidth(), delete.getMeasuredHeight());
    }

    /**
     * 获取两个子View
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        delete = getChildAt(0);
        content = getChildAt(1);
    }


    /**
     * viewDragHelper
     */
    ViewDragHelper.Callback mCallBack = new ViewDragHelper.Callback() {

        //子view
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == content || child == delete;
        }

        //限制移动
        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        //修正view水平位置
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            //限制content的位置
            if(child==content){
                if(left>=0){
                    left=0;
                }else if(left<=-delete.getMeasuredWidth()){
                    left=-delete.getMeasuredWidth();
                }
            }else if(child ==delete){
                //限制delete的位置
                if(left<=content.getMeasuredWidth()-delete.getMeasuredWidth()){
                    left=content.getMeasuredWidth()-delete.getMeasuredWidth();
                }else if(left>=content.getMeasuredWidth()){
                    left=content.getMeasuredWidth();
                }
            }
            return left;
        }

        //当view移动的时候
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            //移动content联动delete,有多种方式
            if(changedView==content){
                int L = content.getLeft()+content.getMeasuredWidth();
                delete.layout(content.getLeft()+content.getMeasuredWidth(),0,L+delete.getMeasuredWidth(),delete.getMeasuredHeight());
            }else if(changedView==delete){
                int R = delete.getLeft();
                content.layout(R-content.getMeasuredWidth(),0,R,content.getMeasuredHeight());
            }


            //回调接口的方法
            if(content.getLeft()==0){
                boolean b = listener!=null;
                Log.d(TAG, "onViewPositionChanged: "+b);
                if(listener!=null){
                    listener.onClose(SwipeLayout.this);
                    Log.d(TAG, "onViewPositionChanged: 调用onclose");
                }
            }else if(content.getLeft()==-delete.getMeasuredWidth()){
                if(listener!=null){
                    listener.onOpen(SwipeLayout.this);
                }
            }

        }

        //当手指抬起
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);

            if(content.getLeft()>-delete.getMeasuredWidth()/2){
                //向右关闭
                close();
            }else{
                //向左打开
                open();
            }
        }
    };

    /**
     * 关闭侧栏
     */
    public void close() {
        viewDragHelper.smoothSlideViewTo(content,0,0);
        ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
    }

    /**
     * 打开侧栏
     */
    public void open() {
        viewDragHelper.smoothSlideViewTo(content,-delete.getMeasuredWidth(),0);
        ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(viewDragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
        }
    }

    /**
     * 设置监听回调
     */
    private onSwipeListener listener;

    public void setonSwipeListener(onSwipeListener listener){
        this.listener = listener;
    }

    public interface onSwipeListener{
        void onOpen(SwipeLayout swipeLayout);
        void onClose(SwipeLayout swipeLayout);
        void onTouchDown(SwipeLayout swipeLayout);
    }

}
