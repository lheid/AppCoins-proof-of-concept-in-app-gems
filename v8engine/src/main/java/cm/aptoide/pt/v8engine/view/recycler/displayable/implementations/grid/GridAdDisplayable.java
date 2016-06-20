/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 20/06/2016.
 */

package cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid;

import cm.aptoide.pt.model.v2.GetAdsResponse;
import cm.aptoide.pt.model.v7.Type;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.view.recycler.displayable.DisplayablePojo;

/**
 * Created by neuro on 20-06-2016.
 */
public class GridAdDisplayable extends DisplayablePojo<GetAdsResponse.Ad> {
	
	@Override
	public Type getType() {
		return Type.ADS;
	}

	@Override
	public int getViewLayout() {
		return R.layout.displayable_grid_sponsored;
	}
}