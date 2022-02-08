package kniezrec.com.flightinfo.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Base class that all presenters should implement. */
public abstract class BaseViewPresenter<V> {

  private @Nullable V mView;

  public final void attachView(V view) {
    if (view == null) {
      return;
    }

    mView = view;
    onViewAttached();
  }

  protected void onViewAttached() {
  }

  public final void detachView() {
    onViewDetached();
    mView = null;
  }

  protected void onViewDetached() {
  }

  public @Nullable
  V getView() {
    return mView;
  }

  public @NonNull
  V getRequiredNotNullView() {
    if (mView == null) {
      throw new NullPointerException("View cannot be null!");
    }

    return mView;
  }
}
