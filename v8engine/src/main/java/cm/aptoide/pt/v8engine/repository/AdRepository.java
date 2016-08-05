/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 04/08/2016.
 */

package cm.aptoide.pt.v8engine.repository;

import java.util.List;

import cm.aptoide.pt.dataprovider.model.MinimalAd;
import cm.aptoide.pt.dataprovider.ws.v2.aptwords.GetAdsRequest;
import cm.aptoide.pt.model.v2.GetAdsResponse;
import rx.Observable;

/**
 * Created by marcelobenites on 7/27/16.
 */
public class AdRepository {

	public static boolean validAds(List<GetAdsResponse.Ad> ads) {
		return ads != null && !ads.isEmpty() && ads.get(0) != null && ads.get(0).getPartner() != null && ads.get(0).getPartner().getData() != null;
	}

	public static boolean validAds(GetAdsResponse getAdsResponse) {
		return getAdsResponse != null && validAds(getAdsResponse.getAds());
	}

	public Observable<MinimalAd> getAdFromAppView(String packageName, String storeName) {
		return GetAdsRequest.ofAppviewOrganic(packageName, storeName).observe().map(response -> response.getAds()).flatMap(ads -> {
			if (!validAds(ads)) {
				return Observable.error(new IllegalStateException("Invalid ads returned from server"));
			}
			return Observable.just(ads.get(0));
		}).map(ad -> MinimalAd.from(ad));
	}
}