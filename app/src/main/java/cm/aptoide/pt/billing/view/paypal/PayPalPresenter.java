package cm.aptoide.pt.billing.view.paypal;

import android.os.Bundle;
import cm.aptoide.pt.billing.Billing;
import cm.aptoide.pt.billing.BillingAnalytics;
import cm.aptoide.pt.billing.Product;
import cm.aptoide.pt.billing.product.InAppProduct;
import cm.aptoide.pt.billing.product.PaidAppProduct;
import cm.aptoide.pt.billing.view.BillingNavigator;
import cm.aptoide.pt.presenter.Presenter;
import cm.aptoide.pt.presenter.View;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import rx.Completable;
import rx.Scheduler;

public class PayPalPresenter implements Presenter {

  private static final int PAY_APP_REQUEST_CODE = 12;
  private final PayPalView view;
  private final Billing billing;
  private final BillingAnalytics analytics;
  private final BillingNavigator billingNavigator;
  private final String paymentMethodName;
  private final Scheduler viewScheduler;
  private final String sellerId;
  private final String productId;
  private final String developerPayload;

  public PayPalPresenter(PayPalView view, Billing billing, BillingAnalytics analytics,
      BillingNavigator billingNavigator, Scheduler viewScheduler, String sellerId, String productId,
      String developerPayload, String paymentMethodName) {
    this.view = view;
    this.billing = billing;
    this.analytics = analytics;
    this.billingNavigator = billingNavigator;
    this.paymentMethodName = paymentMethodName;
    this.viewScheduler = viewScheduler;
    this.sellerId = sellerId;
    this.productId = productId;
    this.developerPayload = developerPayload;
  }

  @Override public void present() {

    onViewCreatedShowPayPalPayment();

    handlePayPalResultEvent();

    handleErrorDismissEvent();
  }

  @Override public void saveState(Bundle state) {

  }

  @Override public void restoreState(Bundle state) {

  }

  private void onViewCreatedShowPayPalPayment() {
    view.getLifecycle()
        .first(event -> event.equals(View.LifecycleEvent.RESUME))
        .flatMapSingle(created -> billing.getProduct(sellerId, productId))
        .delay(100, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .doOnNext(product -> billingNavigator.navigateToPayPalForResult(PAY_APP_REQUEST_CODE,
            product.getPrice()
                .getCurrency(), getPaymentDescription(product), product.getPrice()
                .getAmount()))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> hideLoadingAndShowError(throwable));
  }

  private void handlePayPalResultEvent() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> billingNavigator.payPalResults(PAY_APP_REQUEST_CODE)
            .doOnNext(result -> view.showLoading())
            .flatMapCompletable(result -> processPayPalPayment(result).observeOn(viewScheduler)
                .doOnCompleted(() -> {
                  view.hideLoading();
                  billingNavigator.popView();
                })))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> hideLoadingAndShowError(throwable));
  }

  private void handleErrorDismissEvent() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.errorDismisses())
        .doOnNext(product -> {
          analytics.sendAuthorizationErrorEvent(paymentMethodName);
          billingNavigator.popView();
        })
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> hideLoadingAndShowError(throwable));
  }

  private Completable processPayPalPayment(BillingNavigator.PayPalResult result) {
    switch (result.getStatus()) {
      case BillingNavigator.PayPalResult.SUCCESS:
        analytics.sendAuthorizationSuccessEvent(paymentMethodName);
        return billing.processLocalPayment(sellerId, productId, developerPayload,
            result.getPaymentConfirmationId());
      case BillingNavigator.PayPalResult.CANCELLED:
        analytics.sendAuthorizationCancelEvent(paymentMethodName);
        return Completable.complete();
      case BillingNavigator.PayPalResult.ERROR:
        analytics.sendAuthorizationErrorEvent(paymentMethodName);
      default:
        return Completable.complete();
    }
  }

  private String getPaymentDescription(Product product) {
    if (product instanceof InAppProduct) {
      return String.format(Locale.US, "%s - %s", ((InAppProduct) product).getApplicationName(),
          product.getTitle());
    } else if (product instanceof PaidAppProduct) {
      return product.getTitle();
    }
    throw new IllegalArgumentException(
        "Can NOT provide PayPal payment description. Unknown product.");
  }

  private void hideLoadingAndShowError(Throwable throwable) {
    view.hideLoading();

    if (throwable instanceof IOException) {
      view.showNetworkError();
    } else {
      view.showUnknownError();
    }
  }
}