package kniezrec.com.flightinfo.common.snackbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.ColorRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListenerAdapter;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.google.android.material.behavior.SwipeDismissBehavior;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import kniezrec.com.flightinfo.R;

public final class TopSnackbar {

  static abstract class Callback {
    static final int DISMISS_EVENT_ACTION = 1;
    static final int DISMISS_EVENT_TIMEOUT = 2;
    static final int DISMISS_EVENT_MANUAL = 3;
    static final int DISMISS_EVENT_CONSECUTIVE = 4;

    @IntDef({DISMISS_EVENT_ACTION, DISMISS_EVENT_TIMEOUT, DISMISS_EVENT_MANUAL, DISMISS_EVENT_CONSECUTIVE})

    @Retention(RetentionPolicy.SOURCE)
    @interface DismissEvent {
    }

    void onDismissed(TopSnackbar snackbar) {
    }

    void onShown(TopSnackbar snackbar) {
    }
  }

  @IntDef({LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG, LENGTH_VERY_LONG})
  @Retention(RetentionPolicy.SOURCE)
  @interface Duration {
  }

  public static final int LENGTH_LONG = 0;
  public static final int LENGTH_SHORT = -1;
  public static final int LENGTH_INDEFINITE = -2;
  public static final int LENGTH_VERY_LONG = -3;

  private static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();

  private static final int ANIMATION_DURATION = 250;
  private static final int ANIMATION_FADE_DURATION = 180;

  private static final Handler sHandler;

  private static final int MSG_SHOW = 0;
  private static final int MSG_DISMISS = 1;

  static {
    sHandler = new Handler(Looper.getMainLooper(), message -> {
      switch (message.what) {
        case MSG_SHOW:
          ((TopSnackbar) message.obj).showView();
          return true;
        case MSG_DISMISS:
          ((TopSnackbar) message.obj).hideView();
          return true;
      }
      return false;
    });
  }

  private final ViewGroup mParent;
  private final Context mContext;
  private final SnackbarLayout mView;

  private int mDuration;

  private TopSnackbar(ViewGroup parent) {
    mParent = parent;
    mContext = parent.getContext();
    final LayoutInflater inflater = LayoutInflater.from(mContext);
    mView = (SnackbarLayout) inflater.inflate(R.layout.top_snackbar_layout, mParent, false);
  }

  @NonNull
  public static TopSnackbar make(@NonNull View view, @NonNull CharSequence text, @Duration int duration) {
    final TopSnackbar snackbar = new TopSnackbar(findSuitableParent(view));
    snackbar.setText(text);
    snackbar.setDuration(duration);
    return snackbar;
  }

  @NonNull
  public static TopSnackbar make(@NonNull View view, @StringRes int resId, @Duration int duration) {
    return make(view, view.getResources().getText(resId), duration);
  }

  private static ViewGroup findSuitableParent(View view) {
    ViewGroup fallback = null;
    do {
      if (view instanceof CoordinatorLayout) {
        return (ViewGroup) view;
      } else if (view instanceof FrameLayout) {
        if (view.getId() == android.R.id.content) {
          return (ViewGroup) view;
        } else {
          fallback = (ViewGroup) view;
        }
      } else if (isToolbarInstance(view)) {
                /*
                    If we return the toolbar here, the toast will be attached inside the toolbar.
                    So we need to find a some sibling ViewGroup to the toolbar that comes after the toolbar
                    If we don't find such view, the toast will be attached to the root activity view
                 */
        if (view.getParent() instanceof ViewGroup) {
          ViewGroup parent = (ViewGroup) view.getParent();

          // check if there's something else beside toolbar
          if (parent.getChildCount() > 1) {
            int childrenCnt = parent.getChildCount();
            int toolbarIdx = 0;
            for (int i = 0; i < childrenCnt; i++) {
              // find the index of toolbar in the layout (most likely 0, but who knows)
              if (parent.getChildAt(i) == view) {
                toolbarIdx = i;
                // check if there's something else after the toolbar in the layout
                if (toolbarIdx < childrenCnt - 1) {
                  //try to find some ViewGroup where you can attach the toast
                  while (i < childrenCnt) {
                    i++;
                    View v = parent.getChildAt(i);
                    if (v instanceof ViewGroup) return (ViewGroup) v;
                  }
                }
                break;
              }
            }
          }
        }
      }

      if (view != null) {
        final ViewParent parent = view.getParent();
        view = parent instanceof View ? (View) parent : null;
      }
    } while (view != null);

    return fallback;
  }

  private static boolean isToolbarInstance(View view) {
    final boolean isSupportToolbar = view instanceof androidx.appcompat.widget.Toolbar;
    return isSupportToolbar || view instanceof Toolbar;
  }

  /**
   * Overrides the max width of this snackbar's layout. This is typically not necessary; the snackbar
   * width will be according to Google's Material guidelines. Specifically, the max width will be
   * <p>
   * To allow the snackbar to have a width equal to the parent view, set a value <= 0.
   *
   * @param maxWidth the max width in pixels
   * @return this TopSnackbar
   */
  public TopSnackbar setMaxWidth(int maxWidth) {
    mView.mMaxWidth = maxWidth;
    return this;
  }

  @NonNull
  public TopSnackbar setText(@NonNull CharSequence message) {
    final TextView tv = mView.getMessageView();
    tv.setText(message);
    return this;
  }


  @NonNull
  public TopSnackbar setText(@StringRes int resId) {
    return setText(mContext.getText(resId));
  }

  @NonNull
  public TopSnackbar setDuration(@Duration int duration) {
    mDuration = duration;
    return this;
  }

  @Duration
  public int getDuration() {
    return mDuration;
  }

  @NonNull
  public View getView() {
    return mView;
  }

  public void setBackgroundColor(@ColorRes int color) {
    mView.setBackgroundResource(color);
  }

  public void setAction(@StringRes int textRes, View.OnClickListener listener) {
    final Button button = mView.getActionView();
    button.setText(textRes);
    button.setOnClickListener(listener);
    button.setVisibility(View.VISIBLE);
  }

  public void show() {
    TopSnackbarController.getInstance()
        .show(mDuration, mManagerCallback);
  }

  public void dismiss() {
    dispatchDismiss(Callback.DISMISS_EVENT_MANUAL);
  }

  private void dispatchDismiss(@Callback.DismissEvent int event) {
    TopSnackbarController.getInstance().dismiss(mManagerCallback, event);
  }

  public boolean isShown() {
    return TopSnackbarController.getInstance().isCurrent(mManagerCallback);
  }

  public boolean isShownOrQueued() {
    return TopSnackbarController.getInstance().isCurrentOrNext(mManagerCallback);
  }

  private final TopSnackbarController.Callback mManagerCallback = new TopSnackbarController.Callback() {
    @Override
    public void show() {
      sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, TopSnackbar.this));
    }

    @Override
    public void dismiss(int event) {
      sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, event, 0, TopSnackbar.this));
    }
  };

  final void showView() {
    if (mView.getParent() == null) {
      mParent.addView(mView);
    }

    mView.setOnAttachStateChangeListener(new SnackbarLayout.OnAttachStateChangeListener() {
      @Override
      public void onViewAttachedToWindow(View v) {
      }

      @Override
      public void onViewDetachedFromWindow(View v) {
        if (isShownOrQueued()) {
          sHandler.post(new Runnable() {
            @Override
            public void run() {
              onViewHidden();
            }
          });
        }
      }
    });

    if (ViewCompat.isLaidOut(mView)) {
      animateViewIn();
    } else {
      mView.setOnLayoutChangeListener(new SnackbarLayout.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View view, int left, int top, int right, int bottom) {
          animateViewIn();
          mView.setOnLayoutChangeListener(null);
        }
      });
    }
  }

  private void animateViewIn() {
    mView.setTranslationY(-mView.getHeight());
    ViewCompat.animate(mView)
        .translationY(0f)
        .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
        .setDuration(ANIMATION_DURATION)
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(View view) {
            mView.animateChildrenIn(ANIMATION_DURATION - ANIMATION_FADE_DURATION,
                ANIMATION_FADE_DURATION);
          }

          @Override
          public void onAnimationEnd(View view) {
            TopSnackbarController.getInstance().onShown(mManagerCallback);
          }
        })
        .start();
  }

  private void animateViewOut() {
    ViewCompat.animate(mView)
        .translationY(-mView.getHeight())
        .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
        .setDuration(ANIMATION_DURATION)
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(View view) {
            mView.animateChildrenOut(0, ANIMATION_FADE_DURATION);
          }

          @Override
          public void onAnimationEnd(View view) {
            onViewHidden();
          }
        })
        .start();
  }

  private void hideView() {
    if (mView.getVisibility() != View.VISIBLE || isBeingDragged()) {
      onViewHidden();
    } else {
      animateViewOut();
    }
  }

  private void onViewHidden() {
    TopSnackbarController.getInstance().onDismissed(mManagerCallback);

    final ViewParent parent = mView.getParent();
    if (parent instanceof ViewGroup) {
      ((ViewGroup) parent).removeView(mView);
    }
  }

  private boolean isBeingDragged() {
    final ViewGroup.LayoutParams lp = mView.getLayoutParams();

    if (lp instanceof CoordinatorLayout.LayoutParams) {
      final CoordinatorLayout.LayoutParams cllp = (CoordinatorLayout.LayoutParams) lp;
      final CoordinatorLayout.Behavior behavior = cllp.getBehavior();

      if (behavior instanceof SwipeDismissBehavior) {
        return ((SwipeDismissBehavior) behavior).getDragState()
            != SwipeDismissBehavior.STATE_IDLE;
      }
    }
    return false;
  }

  public static class SnackbarLayout extends LinearLayout {
    interface OnLayoutChangeListener {
      void onLayoutChange(View view, int left, int top, int right, int bottom);
    }

    interface OnAttachStateChangeListener {
      void onViewAttachedToWindow(View v);

      void onViewDetachedFromWindow(View v);
    }

    private TextView mMessageView;
    private Button mActionView;
    private int mMaxWidth;
    private final int mMaxInlineActionWidth;
    private OnLayoutChangeListener mOnLayoutChangeListener;
    private OnAttachStateChangeListener mOnAttachStateChangeListener;

    public SnackbarLayout(Context context) {
      this(context, null);
    }

    public SnackbarLayout(Context context, AttributeSet attrs) {
      super(context, attrs);
      final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);
      mMaxWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_android_maxWidth, -1);
      mMaxInlineActionWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_maxActionInlineWidth, -1);
      if (a.hasValue(R.styleable.SnackbarLayout_elevation)) {
        ViewCompat.setElevation(this, a.getDimensionPixelSize(
            R.styleable.SnackbarLayout_elevation, 0));
      }
      a.recycle();
      LayoutInflater.from(context).inflate(R.layout.top_snackbar_layout_include, this);
      ViewCompat.setAccessibilityLiveRegion(this, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
    }

    @Override
    protected void onFinishInflate() {
      super.onFinishInflate();
      mMessageView = findViewById(R.id.snackbar_text);
      mActionView = findViewById(R.id.snackbar_action);
    }

    TextView getMessageView() {
      return mMessageView;
    }

    Button getActionView() {
      return mActionView;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);

      if (mMaxWidth > 0 && getMeasuredWidth() > mMaxWidth) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      }

      final int multiLineVPadding = getResources().getDimensionPixelSize(R.dimen.design_snackbar_padding_vertical_2lines);
      final int singleLineVPadding = getResources().getDimensionPixelSize(R.dimen.design_snackbar_padding_vertical);
      final boolean isMultiLine = mMessageView.getLayout().getLineCount() > 1;

      boolean remeasure = false;
      if (isMultiLine && mMaxInlineActionWidth > 0
          && mActionView.getMeasuredWidth() > mMaxInlineActionWidth) {
        if (updateViewsWithinLayout(VERTICAL, multiLineVPadding,
            multiLineVPadding - singleLineVPadding)) {
          remeasure = true;
        }
      } else {
        final int messagePadding = isMultiLine ? multiLineVPadding : singleLineVPadding;
        if (updateViewsWithinLayout(HORIZONTAL, messagePadding, messagePadding)) {
          remeasure = true;
        }
      }

      if (remeasure) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      }
    }

    void animateChildrenIn(int delay, int duration) {
      mMessageView.setAlpha(0F);
      ViewCompat.animate(mMessageView)
          .alpha(1f)
          .setDuration(duration)
          .setStartDelay(delay)
          .start();

      if (mActionView.getVisibility() == VISIBLE) {
        mActionView.setAlpha(0F);
        ViewCompat.animate(mActionView)
            .alpha(1f)
            .setDuration(duration)
            .setStartDelay(delay)
            .start();
      }
    }

    void animateChildrenOut(int delay, int duration) {
      mMessageView.setAlpha(1F);
      ViewCompat.animate(mMessageView)
          .alpha(0f)
          .setDuration(duration)
          .setStartDelay(delay)
          .start();

      if (mActionView.getVisibility() == VISIBLE) {
        mActionView.setAlpha(1F);
        ViewCompat.animate(mActionView)
            .alpha(0f)
            .setDuration(duration)
            .setStartDelay(delay)
            .start();
      }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
      super.onLayout(changed, l, t, r, b);
      if (changed && mOnLayoutChangeListener != null) {
        mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
      }
    }

    @Override
    protected void onAttachedToWindow() {
      super.onAttachedToWindow();
      if (mOnAttachStateChangeListener != null) {
        mOnAttachStateChangeListener.onViewAttachedToWindow(this);
      }
    }

    @Override
    protected void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      if (mOnAttachStateChangeListener != null) {
        mOnAttachStateChangeListener.onViewDetachedFromWindow(this);
      }
    }

    void setOnLayoutChangeListener(OnLayoutChangeListener onLayoutChangeListener) {
      mOnLayoutChangeListener = onLayoutChangeListener;
    }

    void setOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
      mOnAttachStateChangeListener = listener;
    }

    private boolean updateViewsWithinLayout(final int orientation,
                                            final int messagePadTop, final int messagePadBottom) {
      boolean changed = false;
      if (orientation != getOrientation()) {
        setOrientation(orientation);
        changed = true;
      }
      if (mMessageView.getPaddingTop() != messagePadTop
          || mMessageView.getPaddingBottom() != messagePadBottom) {
        updateTopBottomPadding(mMessageView, messagePadTop, messagePadBottom);
        changed = true;
      }
      return changed;
    }

    private static void updateTopBottomPadding(View view, int topPadding, int bottomPadding) {
      if (ViewCompat.isPaddingRelative(view)) {
        ViewCompat.setPaddingRelative(view,
            ViewCompat.getPaddingStart(view), topPadding,
            ViewCompat.getPaddingEnd(view), bottomPadding);
      } else {
        view.setPadding(view.getPaddingLeft(), topPadding,
            view.getPaddingRight(), bottomPadding);
      }
    }
  }
}