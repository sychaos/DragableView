package cn.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

public class DragableListener implements View.OnTouchListener {
    // Cached ViewConfiguration and system-wide constant values
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime;

    // Fixed properties
    private View mView;
    private DismissCallbacks mCallbacks;
    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero
    private int mViewHeight = 1;

    // Transient properties
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private VelocityTracker mVelocityTracker;
    private float mTranslationX;
    private float mTranslationY;

    private boolean mTiltEnabled = true;

    public interface DismissCallbacks {

        boolean canSwipe();

        void onDismiss(View view, boolean toRight);

        void onSwiping(float degree);
    }

    public DragableListener(View view, DismissCallbacks callbacks) {
        ViewConfiguration vc = ViewConfiguration.get(view.getContext());

        //getScaledTouchSlop是一个距离，表示滑动的时候，
        // 手的移动要大于这个距离才开始移动控件。如果小于这个距离就不触发移动控件，如viewpager就是用这个距离来判断用户是否翻页
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = view.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mView = view;
        mCallbacks = callbacks;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // offset because the view is translated during swipe
        motionEvent.offsetLocation(mTranslationX, mTranslationY);

        if (mViewWidth < 2) {
            mViewWidth = mView.getWidth();
        }

        if (mViewHeight < 2) {
            mViewHeight = mView.getHeight();
        }

        switch (motionEvent.getActionMasked()) {
            // 一次ACTION_DOWN和ACTION_UP消费一次motionEvent
            case MotionEvent.ACTION_DOWN: {
                // 每一次手指按下，记为实例化一个 VelocityTracker
                // VelocityTracker 是一个跟踪触摸事件滑动速度的帮助类，通过addMovement分析MotionEvent在单位时间类发生的位移来计算速度。
                mDownX = motionEvent.getRawX();
                mDownY = motionEvent.getRawY();
                // 这个token并木有用到
                if (mCallbacks.canSwipe()) {
                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(motionEvent);
                }
                return false;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    mView.performClick();
                    break;
                }

                //mDownX为起点，motionEvent.getRawX()为终点 deltaX为以前按压事件移动的距离
                float deltaX = motionEvent.getRawX() - mDownX;
                float deltaY = motionEvent.getRawY() - mDownY;
                mVelocityTracker.addMovement(motionEvent);
                //  VelocityTracker先调用computeCurrentVelocity(int)来初始化速率的单位 。
                mVelocityTracker.computeCurrentVelocity(1000);
                //  横向速度
                float velocityX = mVelocityTracker.getXVelocity();
                float absVelocityX = Math.abs(velocityX);
                float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
                boolean dismiss = false;
                final boolean dismissRight;
                final boolean dismissTop;
                // 当移动距离大于屏幕的ViewWidth的2分之一 并且mSwiping时 进行dimiss操作
                if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
                    dismiss = true;
                    // 如果deltaX > 0 向右dimiss
                    dismissRight = deltaX > 0;
                    dismissTop = deltaY > 0;
                    // 当横轴移动速率大于最小速率并且大于最大速率时
                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
                        && absVelocityY < absVelocityX
                        && absVelocityY < absVelocityX && mSwiping) {
                    // 当速率方向与移动方向一致时 进行dimiss
                    dismiss = (velocityX < 0) == (deltaX < 0);
                    // 当速率>0时 向右dimiss
                    dismissRight = mVelocityTracker.getXVelocity() > 0;
                    dismissTop = mVelocityTracker.getYVelocity() > 0;
                } else {
                    dismissRight = false;
                    dismissTop = false;
                }

                //mTiltEnabled设置是否旋转
                if (dismiss) {
                    // dismiss
                    mView.animate()
                            .translationX(dismissRight ? mViewWidth : -mViewWidth)
                            .translationY(dismissTop ? mViewHeight : -mViewHeight)
                            .rotation(mTiltEnabled ? (dismissRight ? 45 : -45) : 0f)
                            .setDuration(mAnimationTime)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    performDismiss(dismissRight);
                                }
                            });
                    mCallbacks.onSwiping(0);
                    // mSwiping指翻滚中 翻滚状态下如果不满足dimiss的操作 会停止动画 还原
                } else if (mSwiping) {
                    // cancel
                    mView.animate()
                            .translationX(0)
                            .translationY(0)
                            .rotation(0)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                    mCallbacks.onSwiping(1);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mTranslationX = 0;
                mTranslationY = 0;
                mDownX = 0;
                mDownY = 0;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                //如果没走ACTION_DOWN 无视他
                if (mVelocityTracker == null) {
                    mView.performClick();
                    break;
                }

                //停止动画 还原
                mView.animate()
                        .translationX(0)
                        .translationY(0)
                        .rotation(0)
                        .setDuration(mAnimationTime)
                        .setListener(null);
                mCallbacks.onSwiping(1);
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mTranslationX = 0;
                mTranslationY = 0;
                mDownX = 0;
                mDownY = 0;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                //如果没走ACTION_DOWN 无视他
                if (mVelocityTracker == null) {
                    mView.performClick();
                    break;
                }

                mVelocityTracker.addMovement(motionEvent);
                float deltaX = motionEvent.getRawX() - mDownX;
                float deltaY = motionEvent.getRawY() - mDownY;
                //如果x轴移动距离大于 离开控件的距离 并且Y轴移动小于x轴二分之一
                if (Math.abs(deltaX) > mSlop
                        || Math.abs(deltaY) > mSlop) {
                    mSwiping = true;
                    mView.getParent().requestDisallowInterceptTouchEvent(true);

                    // Cancel listview's touch
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex() <<
                                    MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }
                // 设置拖拽中的动画
                if (mSwiping) {
                    mTranslationX = deltaX;
                    mTranslationY = deltaY;
                    //设置左右偏移量
                    mView.setTranslationX(deltaX);
                    mView.setTranslationY(deltaY);
                    //设置翻滚角度
                    mView.setRotation(mTiltEnabled ? 45f * deltaX / mViewWidth : 0f);
                    //设置透明度
                    float minAlpha = Math.min(1f - 2f * Math.abs(deltaY) / mViewHeight, 1f - 2f * Math.abs(deltaX) / mViewWidth);

                    mCallbacks.onSwiping(Math.max(0f, Math.min(1f, minAlpha)));
                    return true;
                }
                break;
            }
        }
        return false;
    }

    // 执行dimiss操作
    private void performDismiss(final boolean toRight) {
        final ViewGroup.LayoutParams lp = mView.getLayoutParams();
        final int originalHeight = mView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //回调
                mCallbacks.onDismiss(mView, toRight);
                //重置View 如果你在onDismiss中dimiss了dialog是看不到效果的
                mView.setTranslationX(0);
                mView.setTranslationY(0);
                mView.setRotation(0);
                lp.height = originalHeight;
                mView.setLayoutParams(lp);
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                mView.setLayoutParams(lp);
            }
        });

        animator.start();
    }
}
