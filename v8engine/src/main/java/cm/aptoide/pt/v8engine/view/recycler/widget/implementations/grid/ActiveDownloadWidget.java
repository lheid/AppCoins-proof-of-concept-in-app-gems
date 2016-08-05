package cm.aptoide.pt.v8engine.view.recycler.widget.implementations.grid;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import cm.aptoide.pt.database.realm.Download;
import cm.aptoide.pt.imageloader.ImageLoader;
import cm.aptoide.pt.utils.AptoideUtils;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.ActiveDownloadDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.widget.Displayables;
import cm.aptoide.pt.v8engine.view.recycler.widget.Widget;
import rx.Subscription;

/**
 * Created by trinkes on 7/18/16.
 */
@Displayables({ActiveDownloadDisplayable.class})
public class ActiveDownloadWidget extends Widget<ActiveDownloadDisplayable> {

	private TextView appName;
	private ProgressBar progressBar;
	private TextView downloadSpeedTv;
	private TextView downloadProgressTv;
	private ImageView pauseCancelButton;
	private ImageView appIcon;
	private Subscription subscribe;
	private Download download;
	private ActiveDownloadDisplayable displayable;

	public ActiveDownloadWidget(View itemView) {
		super(itemView);
	}

	@Override
	protected void assignViews(View itemView) {
		appName = (TextView) itemView.findViewById(R.id.app_name);
		downloadSpeedTv = (TextView) itemView.findViewById(R.id.speed);
		downloadProgressTv = (TextView) itemView.findViewById(R.id.progress);
		progressBar = (ProgressBar) itemView.findViewById(R.id.downloading_progress);
		pauseCancelButton = (ImageView) itemView.findViewById(R.id.pause_cancel_button);
		appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
	}

	@Override
	public void bindView(ActiveDownloadDisplayable displayable) {
		this.displayable = displayable;
		download = displayable.getPojo();
		appName.setText(download.getAppName());
		if (!TextUtils.isEmpty(download.getIcon())) {
			ImageLoader.load(download.getIcon(), appIcon);
		}
		if (download.getOverallDownloadStatus() == Download.IN_QUEUE) {
			progressBar.setIndeterminate(true);
		} else {
			progressBar.setIndeterminate(false);
			progressBar.setProgress(download.getOverallProgress());
		}
		downloadProgressTv.setText(download.getOverallProgress() + "%");
		downloadSpeedTv.setText(String.valueOf(AptoideUtils.StringU.formatBits((long) download.getDownloadSpeed())));

		if (subscribe != null && subscribe.isUnsubscribed()) {
			subscribe.unsubscribe();
		}
		subscribe = RxView.clicks(pauseCancelButton).subscribe(aVoid -> {
			displayable.pauseInstall(download);
		});
	}

	@Override
	public void onViewAttached() {
		if (subscribe == null) {
			subscribe = RxView.clicks(pauseCancelButton).subscribe(click -> displayable.pauseInstall(download));
		}

	}

	@Override
	public void onViewDetached() {

	}
}