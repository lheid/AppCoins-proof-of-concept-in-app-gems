/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 09/05/2016.
 */

package cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.appView;

import cm.aptoide.pt.model.v7.Type;

/**
 * Created by sithengineer on 04/05/16.
 */
public class AppViewDeveloperDisplayable extends AppViewDisplayable<Object> {

	public AppViewDeveloperDisplayable() {
	}

	@Override
	public Type getType() {
		return Type.APP_VIEW_DEVELOPER;
	}

	@Override
	public int getViewLayout() {
		return 0;
	}
}
