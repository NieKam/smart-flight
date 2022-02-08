package kniezrec.com.flightinfo.cards.base;

import android.os.Messenger;

import java.util.List;

import androidx.annotation.NonNull;
import kniezrec.com.flightinfo.base.BaseViewPresenter;
import kniezrec.com.flightinfo.services.LocationService;
import kniezrec.com.flightinfo.services.SensorService;

/**
 * Copyright by Kamil Niezrecki
 */
public class ServiceBasedCardPresenter<VIEW_CONTRACT extends ServiceBasedCardPresenter.ViewContract>
    extends BaseViewPresenter<VIEW_CONTRACT> {

  protected void onViewAttached(@NonNull VIEW_CONTRACT viewContract) {
  }

  protected void onViewDetached(@NonNull VIEW_CONTRACT viewContract) {
  }

  protected void onLocationServiceConnected(LocationService service) {
  }

  protected void onLocationServiceDisconnected() {
  }

  protected void onSensorServiceConnected(SensorService service) {
  }

  protected void onSensorServiceDisconnected() {
  }

  protected void onCityFinderServiceConnected(Messenger service) {
  }

  protected void onCityFinderServiceDisconnected() {
  }

  @Override
  protected void onViewAttached() {
    super.onViewAttached();
    final VIEW_CONTRACT viewContract = getRequiredNotNullView();
    onViewAttached(viewContract);
  }

  @Override
  protected void onViewDetached() {
    super.onViewDetached();
    final VIEW_CONTRACT viewContract = getRequiredNotNullView();
    onViewDetached(viewContract);
  }

  public interface ViewContract {

    void connectToSensorService();

    void connectToLocationService();

    void connectToCityFindService();

    void disconnectFromSensorService();

    void disconnectFromLocationService();

    void disconnectFromCityFindService();

    boolean areFeaturesSupported(List<String> features);
  }
}
