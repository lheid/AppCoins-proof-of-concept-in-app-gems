package cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.timeline;

import android.content.Context;
import cm.aptoide.pt.dataprovider.ws.v7.SendEventRequest;
import cm.aptoide.pt.model.v7.listapp.App;
import cm.aptoide.pt.model.v7.timeline.StoreLatestApps;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.repository.SocialRepository;
import cm.aptoide.pt.v8engine.repository.TimelineMetricsManager;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.DateCalculator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Created by marcelobenites on 6/17/16.
 */
public class StoreLatestAppsDisplayable extends CardDisplayable {

  @Getter private String storeName;
  @Getter private String avatarUrl;
  @Getter private List<LatestApp> latestApps;
  @Getter private String abUrl;
  private DateCalculator dateCalculator;
  private Date date;
  private TimelineMetricsManager timelineMetricsManager;
  private SocialRepository socialRepository;

  public StoreLatestAppsDisplayable() {
  }

  public StoreLatestAppsDisplayable(StoreLatestApps storeLatestApps,
      String storeName, String avatarUrl, List<LatestApp> latestApps, String abUrl,
      DateCalculator dateCalculator, Date date, TimelineMetricsManager timelineMetricsManager,
      SocialRepository socialRepository) {
    super(storeLatestApps);
    this.storeName = storeName;
    this.avatarUrl = avatarUrl;
    this.latestApps = latestApps;
    this.abUrl = abUrl;
    this.dateCalculator = dateCalculator;
    this.date = date;
    this.timelineMetricsManager = timelineMetricsManager;
    this.socialRepository = socialRepository;
  }

  public static StoreLatestAppsDisplayable from(StoreLatestApps storeLatestApps,
      DateCalculator dateCalculator, TimelineMetricsManager timelineMetricsManager,
      SocialRepository socialRepository) {
    final List<LatestApp> latestApps = new ArrayList<>();
    for (App app : storeLatestApps.getApps()) {
      latestApps.add(new LatestApp(app.getId(), app.getIcon(), app.getPackageName()));
    }
    String abTestingURL = null;

    if (storeLatestApps.getAb() != null
        && storeLatestApps.getAb().getConversion() != null
        && storeLatestApps.getAb().getConversion().getUrl() != null) {
      abTestingURL = storeLatestApps.getAb().getConversion().getUrl();
    }
    return new StoreLatestAppsDisplayable(storeLatestApps, storeLatestApps.getStore().getName(),
        storeLatestApps.getStore().getAvatar(), latestApps, abTestingURL, dateCalculator,
        storeLatestApps.getLatestUpdate(), timelineMetricsManager, socialRepository);
  }

  public String getTimeSinceLastUpdate(Context context) {
    return dateCalculator.getTimeSinceDate(context, date);
  }

  @Override public int getViewLayout() {
    return R.layout.displayable_social_timeline_store_latest_apps;
  }

  public void sendClickEvent(SendEventRequest.Body.Data data, String eventName) {
    timelineMetricsManager.sendEvent(data, eventName);
  }

  @Override public void share(Context context, boolean privacyResult) {
    socialRepository.share(getTimelineCard(), context, privacyResult);
  }

  @EqualsAndHashCode @AllArgsConstructor public static class LatestApp {

    @Getter private final long appId;
    @Getter private final String iconUrl;
    @Getter private final String packageName;
  }
}