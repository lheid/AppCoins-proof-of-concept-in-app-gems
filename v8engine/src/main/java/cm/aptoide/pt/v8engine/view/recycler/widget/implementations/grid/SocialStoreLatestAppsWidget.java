package cm.aptoide.pt.v8engine.view.recycler.widget.implementations.grid;

import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cm.aptoide.pt.dataprovider.ws.v7.SendEventRequest;
import cm.aptoide.pt.imageloader.ImageLoader;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.V8Engine;
import cm.aptoide.pt.v8engine.analytics.Analytics;
import cm.aptoide.pt.v8engine.analytics.AptoideAnalytics.AptoideAnalytics;
import cm.aptoide.pt.v8engine.interfaces.FragmentShower;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.SocialStoreLatestAppsDisplayable;
import com.jakewharton.rxbinding.view.RxView;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jdandrade on 29/11/2016.
 */

public class SocialStoreLatestAppsWidget extends CardWidget<SocialStoreLatestAppsDisplayable> {

  private final String cardType = "Social Latest Apps";
  private final LayoutInflater inflater;
  private TextView title;
  private TextView subtitle;
  private LinearLayout appsContaner;
  private ImageView image;
  private View store;
  private SocialStoreLatestAppsDisplayable displayable;
  private Map<View, Long> apps;
  private Map<Long, String> appsPackages;
  private CardView cardView;

  public SocialStoreLatestAppsWidget(View itemView) {
    super(itemView);
    inflater = LayoutInflater.from(itemView.getContext());
    apps = new HashMap<>();
    appsPackages = new HashMap<>();
  }

  @Override protected void assignViews(View itemView) {
    store = itemView.findViewById(R.id.displayable_social_timeline_store_latest_apps_header);
    title = (TextView) itemView.findViewById(
        R.id.displayable_social_timeline_store_latest_apps_card_title);
    image = (ImageView) itemView.findViewById(
        R.id.displayable_social_timeline_store_latest_apps_card_image);
    subtitle = (TextView) itemView.findViewById(
        R.id.displayable_social_timeline_store_latest_apps_card_subtitle);
    appsContaner = (LinearLayout) itemView.findViewById(
        R.id.displayable_social_timeline_store_latest_apps_container);
    cardView =
        (CardView) itemView.findViewById(R.id.displayable_social_timeline_store_latest_apps_card);
  }

  @Override public void bindView(SocialStoreLatestAppsDisplayable displayable) {
    this.displayable = displayable;
    title.setText(displayable.getStoreName());
    subtitle.setText(displayable.getTimeSinceLastUpdate(getContext()));
    setCardviewMargin(displayable, cardView);
    ImageLoader.loadWithShadowCircleTransform(displayable.getAvatarUrl(), image);

    appsContaner.removeAllViews();
    apps.clear();
    View latestAppView;
    ImageView latestAppIcon;
    for (SocialStoreLatestAppsDisplayable.LatestApp latestApp : displayable.getLatestApps()) {
      latestAppView = inflater.inflate(R.layout.social_timeline_latest_app, appsContaner, false);
      latestAppIcon = (ImageView) latestAppView.findViewById(R.id.social_timeline_latest_app);
      ImageLoader.load(latestApp.getIconUrl(), latestAppIcon);
      appsContaner.addView(latestAppView);
      apps.put(latestAppView, latestApp.getAppId());
      appsPackages.put(latestApp.getAppId(), latestApp.getPackageName());
    }

    for (View app : apps.keySet()) {
      compositeSubscription.add(RxView.clicks(app).subscribe(click -> {
        knockWithSixpackCredentials(displayable.getAbUrl());
        String packageName = appsPackages.get(apps.get(app));
        Analytics.AppsTimeline.clickOnCard(cardType, packageName, Analytics.AppsTimeline.BLANK,
            displayable.getStoreName(), Analytics.AppsTimeline.OPEN_APP_VIEW);
        displayable.sendClickEvent(SendEventRequest.Body.Data.builder()
            .cardType(cardType)
            .source(AptoideAnalytics.SOURCE_APTOIDE)
            .specific(SendEventRequest.Body.Specific.builder()
                .app(packageName)
                .store(displayable.getStoreName())
                .build())
            .build(), AptoideAnalytics.OPEN_APP);
        ((FragmentShower) getContext()).pushFragmentV4(
            V8Engine.getFragmentProvider().newAppViewFragment(apps.get(app)));
      }));
    }

    compositeSubscription.add(RxView.clicks(store).subscribe(click -> {
      knockWithSixpackCredentials(displayable.getAbUrl());
      Analytics.AppsTimeline.clickOnCard(cardType, Analytics.AppsTimeline.BLANK,
          Analytics.AppsTimeline.BLANK, displayable.getStoreName(),
          Analytics.AppsTimeline.OPEN_STORE);
      displayable.sendClickEvent(SendEventRequest.Body.Data.builder()
          .cardType(cardType)
          .source(AptoideAnalytics.SOURCE_APTOIDE)
          .specific(
              SendEventRequest.Body.Specific.builder().store(displayable.getStoreName()).build())
          .build(), AptoideAnalytics.OPEN_STORE);
      ((FragmentShower) getContext()).pushFragmentV4(
          V8Engine.getFragmentProvider().newStoreFragment(displayable.getStoreName()));
    }));
  }
}