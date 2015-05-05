package com.thetonrifles.tags;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.thetonrifles.tags.controls.FlowLayout;
import com.thetonrifles.tags.controls.TagView;

import java.util.Locale;

public class MainActivity extends ActionBarActivity {

    private FrameLayout mAnimLayout;
    private FlowLayout mSelectedLayout;
    private ScrollView mAvailableScrollView;
    private FlowLayout mAvailableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // building ui elements
        mAnimLayout = (FrameLayout) findViewById(R.id.layout_anim);
        mAvailableLayout = (FlowLayout) findViewById(R.id.flow_available);
        mAvailableScrollView = (ScrollView) findViewById(R.id.scroll_flow_available);
        mSelectedLayout = (FlowLayout) findViewById(R.id.flow_selected);

        // adding tags into available layout
        for (Locale locale : Locale.getAvailableLocales()) {
            String countryName = locale.getDisplayCountry();
            if (!countryName.isEmpty()) {
                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                mAvailableLayout.addView(createTagView(countryName), params);
            }
        }
    }

    private View createTagView(String text) {
        final TagView sourceTag = new TagView(this);
        sourceTag.setText(text);
        sourceTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // adding target
                final TagView targetTag = buildTargetTag(sourceTag);
                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                mSelectedLayout.addView(targetTag, params);
                ViewTreeObserver observer = mSelectedLayout.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mSelectedLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        // handle animation
                        handleAnimation(sourceTag, targetTag);
                    }
                });
            }
        });
        return sourceTag;
    }

    private View createCopyTagView(TagView view) {
        TagView copy = new TagView(this);
        copy.setText(view.getText());
        copy.setBackgroundResource(R.drawable.bg_tag_blue);
        return copy;
    }

    private TagView buildTargetTag(TagView source) {
        TagView targetTag = new TagView(this);
        targetTag.setText(source.getText());
        targetTag.setBackgroundResource(R.drawable.bg_tag_blue);
        return targetTag;
    }

    private void handleAnimation(final TagView sourceTag, final TagView targetTag) {
        // determining coords of the source view
        int sourceTop = sourceTag.getTop() - mAvailableScrollView.getScrollY() + mSelectedLayout.getMeasuredHeight();
        int sourceLeft = sourceTag.getLeft();
        // determining coords of the target view
        int targetTop = targetTag.getTop();
        int targetLeft = targetTag.getLeft();
        // building copy tag
        final TagView animTag = (TagView) createCopyTagView(sourceTag);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(sourceLeft, sourceTop, 0, 0);
        mAnimLayout.addView(animTag, params);
        // animating copy tag
        AnimatorSet anim = new AnimatorSet();
        ObjectAnimator x = ObjectAnimator.ofFloat(animTag, "x", sourceLeft, targetLeft);
        ObjectAnimator y = ObjectAnimator.ofFloat(animTag, "y", sourceTop, targetTop);
        anim.playTogether(x, y);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                targetTag.setVisibility(View.INVISIBLE);
                sourceTag.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animTag.setVisibility(View.INVISIBLE);
                targetTag.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        anim.setDuration(300);
        anim.start();
    }

}
