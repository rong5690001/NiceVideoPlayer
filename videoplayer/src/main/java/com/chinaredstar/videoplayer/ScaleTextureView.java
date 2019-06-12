package com.chinaredstar.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;

/**
 * 按比例显示的TextureView
 */
public class ScaleTextureView extends TextureView {

    private static int sScreenHeight = -1;
    private float mVideoWidth;
    private float mVideoHeight;

    public ScaleTextureView(Context context) {
        this(context, null);
    }

    public ScaleTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (sScreenHeight == -1) {
            DisplayMetrics display = context.getResources()
                    .getDisplayMetrics();
            sScreenHeight = display.heightPixels;
        }

    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//
//        Context context = getContext();
//        if (context instanceof Activity) {
//            boolean isFullScreen = (context.getResources().getConfiguration().orientation ==
//                    Configuration.ORIENTATION_LANDSCAPE);
//            float scale = 9f / 16f;
//            if (isFullScreen) {
//                width = (int) (sScreenHeight / scale);
//                height = sScreenHeight;
//            } else {
//                height = (int) (width * scale);
//            }
//        }
//
//        setMeasuredDimension(width, height);
//    }

    /**
     * 设置视频的宽高
     *
     * @param videoWidth
     * @param videoHeight
     */
    public void setVideoWidth(float videoWidth, float videoHeight) {
        mVideoWidth = videoWidth;
        mVideoHeight = videoHeight;
        updateTextureViewSizeCenter();
    }

    //重新计算video的显示位置，让其全部显示并据中
    public void updateTextureViewSizeCenter() {
        if (mVideoWidth == 0 || mVideoHeight == 0) {
            Log.e("ScaleTextureView:", "videoWidth,videoHeight为0");
            return;
        }

        float sx = (float) getMeasuredWidth() / mVideoWidth;
        float sy = (float) getMeasuredHeight() / mVideoHeight;

        Matrix matrix = new Matrix();

        //第1步:把视频区移动到View区,使两者中心点重合.
        matrix.preTranslate((getMeasuredWidth() - mVideoWidth) / 2f, (getMeasuredHeight() - mVideoHeight) / 2f);

        //第2步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        matrix.preScale(mVideoWidth / (float) getMeasuredWidth(), mVideoHeight / (float) getMeasuredHeight());

        //第3步,等比例放大或缩小,直到视频区的一边和View一边相等.如果另一边和view的一边不相等，则留下空隙
        if (sx >= sy) {
            matrix.postScale(sy, sy, getMeasuredWidth() / 2f, getMeasuredHeight() / 2f);
        } else {
            matrix.postScale(sx, sx, getMeasuredWidth() / 2f, getMeasuredHeight() / 2f);
        }

        setTransform(matrix);
        requestLayout();
    }
}
