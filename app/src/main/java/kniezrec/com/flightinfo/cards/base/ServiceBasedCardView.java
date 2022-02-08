package kniezrec.com.flightinfo.cards.base;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Messenger;
import android.util.AttributeSet;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import kniezrec.com.flightinfo.common.Navigation;
import kniezrec.com.flightinfo.services.LocationService;
import kniezrec.com.flightinfo.services.SensorService;

/**
 * Copyright by Kamil Niezrecki
 */
public abstract class ServiceBasedCardView<PRESENTER extends ServiceBasedCardPresenter>
    extends CardView
    implements ServiceBasedCardPresenter.ViewContract, CardItem {

  protected final @NonNull PRESENTER mPresenter;

  private @Nullable ServiceConnection mSensorServiceConnection;
  private @Nullable ServiceConnection mLocationServiceConnection;
  private @Nullable ServiceConnection mCityFindServiceConnection;

  public ServiceBasedCardView(@NonNull Context context) {
    this(context, null);
  }

  public ServiceBasedCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ServiceBasedCardView(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mPresenter = initPresenter();
  }

  public abstract PRESENTER initPresenter();

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    mPresenter.attachView(this);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    mPresenter.detachView();
  }

  @Override
  public void connectToSensorService() {
    if (mSensorServiceConnection != null) {
      return;
    }

    mSensorServiceConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        final SensorService sensorService = ((SensorService.LocalBinder) iBinder).getService();
        mPresenter.onSensorServiceConnected(sensorService);
      }

      @Override
      public void onServiceDisconnected(ComponentName componentName) {
        mPresenter.onSensorServiceDisconnected();
      }
    };

    Navigation.bindToSensorService(getContext(), mSensorServiceConnection);
  }

  @Override
  public void connectToLocationService() {
    if (mLocationServiceConnection != null) {
      return;
    }

    mLocationServiceConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        final LocationService locationService =
            ((LocationService.LocalBinder) iBinder).getService();
        mPresenter.onLocationServiceConnected(locationService);
      }

      @Override
      public void onServiceDisconnected(ComponentName componentName) {
        mPresenter.onLocationServiceDisconnected();
      }
    };

    Navigation.bindToLocationService(getContext(), mLocationServiceConnection);
  }

  @Override
  public void connectToCityFindService() {
    if (mCityFindServiceConnection != null) {
      return;
    }

    mCityFindServiceConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName componentName, IBinder service) {
        mPresenter.onCityFinderServiceConnected(new Messenger(service));
      }

      @Override
      public void onServiceDisconnected(ComponentName componentName) {
        mPresenter.onCityFinderServiceDisconnected();
      }
    };

    Navigation.bindToFindCityService(getContext(), mCityFindServiceConnection);
  }

  @Override
  public void disconnectFromSensorService() {
    doDisconnectFromService(mSensorServiceConnection);
    mSensorServiceConnection = null;
  }

  @Override
  public void disconnectFromLocationService() {
    doDisconnectFromService(mLocationServiceConnection);
    mLocationServiceConnection = null;
  }

  @Override
  public void disconnectFromCityFindService() {
    doDisconnectFromService(mCityFindServiceConnection);
    mCityFindServiceConnection = null;
  }

  @Override
  public boolean areFeaturesSupported(List<String> features) {
    final PackageManager packageManager = getContext().getPackageManager();
    for (String feature : features) {
      if (!packageManager.hasSystemFeature(feature)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean needsLocationPermission() {
    return false;
  }

  private void doDisconnectFromService(ServiceConnection serviceConnection) {
    if (serviceConnection == null) {
      return;
    }

    Navigation.unbindFromService(getContext(), serviceConnection);
  }
}
