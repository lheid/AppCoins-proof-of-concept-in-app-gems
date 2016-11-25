package cm.aptoide.pt.v8engine.view.recycler.widget.implementations.grid;

import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.pt.dataprovider.ws.v7.SendEventRequest;
import cm.aptoide.pt.imageloader.ImageLoader;
import cm.aptoide.pt.logger.Logger;
import cm.aptoide.pt.v8engine.BuildConfig;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.V8Engine;
import cm.aptoide.pt.v8engine.analytics.Analytics;
import cm.aptoide.pt.v8engine.analytics.AptoideAnalytics.AptoideAnalytics;
import cm.aptoide.pt.v8engine.interfaces.FragmentShower;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.SocialArticleDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.widget.Widget;
import com.jakewharton.rxbinding.view.RxView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by jdandrade on 23/11/2016.
 */

public class SocialArticleWidget extends Widget<SocialArticleDisplayable> {

  private final String cardType = "Social Article";
  private TextView title;
  private TextView subtitle;
  private ImageView image;
  private TextView articleTitle;
  private ImageView thumbnail;
  private View url;
  private Button getAppButton;
  private CardView cardView;
  private View articleHeader;
  private SocialArticleDisplayable displayable;
  private TextView relatedTo;
  private LinearLayout like;
  private LinearLayout share;
  private LikeButton likeButton;

  private String appName;
  private String packageName;

  public SocialArticleWidget(View itemView) {
    super(itemView);
  }

  @Override protected void assignViews(View itemView) {
    title = (TextView) itemView.findViewById(R.id.card_title);
    subtitle = (TextView) itemView.findViewById(R.id.card_subtitle);
    image = (ImageView) itemView.findViewById(R.id.card_image);
    articleTitle = (TextView) itemView.findViewById(R.id.partial_social_timeline_thumbnail_title);
    thumbnail = (ImageView) itemView.findViewById(R.id.partial_social_timeline_thumbnail_image);
    url = itemView.findViewById(R.id.partial_social_timeline_thumbnail);
    getAppButton =
        (Button) itemView.findViewById(R.id.partial_social_timeline_thumbnail_get_app_button);
    cardView = (CardView) itemView.findViewById(R.id.card);
    articleHeader = itemView.findViewById(R.id.displayable_social_timeline_article_header);
    relatedTo = (TextView) itemView.findViewById(R.id.partial_social_timeline_thumbnail_related_to);
    like = (LinearLayout) itemView.findViewById(R.id.social_like);
    share = (LinearLayout) itemView.findViewById(R.id.social_share);
    likeButton = (LikeButton) itemView.findViewById(R.id.social_like_test);
  }

  @Override public void bindView(SocialArticleDisplayable displayable) {
    this.displayable = displayable;
    title.setText(displayable.getTitle());
    subtitle.setText(displayable.getTimeSinceLastUpdate(getContext()));
    Typeface typeFace =
        Typeface.createFromAsset(getContext().getAssets(), "fonts/DroidSerif-Regular.ttf");
    articleTitle.setTypeface(typeFace);
    articleTitle.setText(displayable.getArticleTitle());
    setCardviewMargin(displayable);
    ImageLoader.loadWithShadowCircleTransform(displayable.getAvatarUrl(), image);
    ImageLoader.load(displayable.getThumbnailUrl(), thumbnail);
    likeButton.setLiked(false);
    //relatedTo.setText(displayable.getAppRelatedToText(getContext(), appName));

    if (getAppButton.getVisibility() != View.GONE && displayable.isGetApp(appName)) {
      getAppButton.setVisibility(View.VISIBLE);
      getAppButton.setText(displayable.getAppText(getContext(), appName));
      getAppButton.setOnClickListener(view -> ((FragmentShower) getContext()).pushFragmentV4(
          V8Engine.getFragmentProvider().newAppViewFragment(displayable.getAppId())));
    }

    //		CustomTabsHelper.getInstance()
    //				.setUpCustomTabsService(displayable.getLink().getUrl(), getContext());

    url.setOnClickListener(v -> {
      knockWithSixpackCredentials(displayable.getAbUrl());
      displayable.getLink().launch(getContext());
      Analytics.AppsTimeline.clickOnCard(cardType, Analytics.AppsTimeline.BLANK,
          displayable.getArticleTitle(), displayable.getTitle(),
          Analytics.AppsTimeline.OPEN_ARTICLE);
      displayable.sendOpenArticleEvent(SendEventRequest.Body.Data.builder()
          .cardType(cardType)
          .source(displayable.getTitle())
          .specific(SendEventRequest.Body.Specific.builder()
              .url(displayable.getLink().getUrl())
              .app(packageName)
              .build())
          .build(), AptoideAnalytics.OPEN_ARTICLE);
    });

    compositeSubscription.add(displayable.getRelatedToApplication()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(installeds -> {
          if (installeds != null && !installeds.isEmpty()) {
            appName = installeds.get(0).getName();
            packageName = installeds.get(0).getPackageName();
          } else {
            setAppNameToFirstLinkedApp();
          }
          if (appName != null) {
            relatedTo.setText(displayable.getAppRelatedToText(getContext(), appName));
          }
        }, throwable -> {
          setAppNameToFirstLinkedApp();
          if (appName != null) {
            relatedTo.setText(displayable.getAppRelatedToText(getContext(), appName));
          }
          throwable.printStackTrace();
        }));

    compositeSubscription.add(RxView.clicks(articleHeader).subscribe(click -> {
      knockWithSixpackCredentials(displayable.getAbUrl());
      displayable.getDeveloperLink().launch(getContext());
      Analytics.AppsTimeline.clickOnCard(cardType, Analytics.AppsTimeline.BLANK,
          displayable.getArticleTitle(), displayable.getTitle(),
          Analytics.AppsTimeline.OPEN_ARTICLE_HEADER);
      displayable.sendOpenArticleEvent(SendEventRequest.Body.Data.builder()
          .cardType(cardType)
          .source(displayable.getTitle())
          .specific(SendEventRequest.Body.Specific.builder()
              .url(displayable.getDeveloperLink().getUrl())
              .app(packageName)
              .build())
          .build(), AptoideAnalytics.OPEN_BLOG);
    }));

    compositeSubscription.add(RxView.clicks(share).subscribe(click -> {
      displayable.share(getContext());
    }, throwable -> throwable.printStackTrace()));

    compositeSubscription.add(RxView.clicks(like).subscribe(click -> {
    }, (throwable) -> throwable.printStackTrace()));

    likeButton.setOnLikeListener(new OnLikeListener() {
      @Override public void liked(LikeButton likeButton) {
        Toast.makeText(getContext(), "LIKED", Toast.LENGTH_SHORT).show();
      }

      @Override public void unLiked(LikeButton likeButton) {
        Toast.makeText(getContext(), "UNLIKED", Toast.LENGTH_SHORT).show();
      }
    });
  }

  //// TODO: 31/08/16 refactor this out of here
  private void knockWithSixpackCredentials(String url) {
    if (url == null) {
      return;
    }

    String credential = Credentials.basic(BuildConfig.SIXPACK_USER, BuildConfig.SIXPACK_PASSWORD);

    OkHttpClient client = new OkHttpClient();

    Request click = new Request.Builder().url(url).addHeader("authorization", credential).build();

    client.newCall(click).enqueue(new Callback() {
      @Override public void onFailure(Call call, IOException e) {
        Logger.d(this.getClass().getSimpleName(), "sixpack request fail " + call.toString());
      }

      @Override public void onResponse(Call call, Response response) throws IOException {
        Logger.d(this.getClass().getSimpleName(), "knock success");
        response.body().close();
      }
    });
  }

  private void setCardviewMargin(SocialArticleDisplayable displayable) {
    CardView.LayoutParams layoutParams =
        new CardView.LayoutParams(CardView.LayoutParams.WRAP_CONTENT,
            CardView.LayoutParams.WRAP_CONTENT);
    layoutParams.setMargins(displayable.getMarginWidth(getContext(),
        getContext().getResources().getConfiguration().orientation), 0,
        displayable.getMarginWidth(getContext(),
            getContext().getResources().getConfiguration().orientation), 30);
    cardView.setLayoutParams(layoutParams);
  }

  private void setAppNameToFirstLinkedApp() {
    if (!displayable.getRelatedToAppsList().isEmpty()) {
      appName = displayable.getRelatedToAppsList().get(0).getName();
    }
  }
}