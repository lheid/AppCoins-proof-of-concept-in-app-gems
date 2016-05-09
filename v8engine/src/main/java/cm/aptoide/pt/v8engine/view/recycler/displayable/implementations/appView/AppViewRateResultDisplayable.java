/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 09/05/2016.
 */

package cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.appView;

import cm.aptoide.pt.model.v7.Type;

/**
 * Created by sithengineer on 04/05/16.
 */
public class AppViewRateResultDisplayable extends AppViewDisplayable<Object> {

	public AppViewRateResultDisplayable() {
	}

	@Override
	public Type getType() {
		return Type.APP_VIEW_RATE_RESULT;
	}

	@Override
	public int getViewLayout() {
		return 0;
	}
}
