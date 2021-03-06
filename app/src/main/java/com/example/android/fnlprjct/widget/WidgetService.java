package com.example.android.fnlprjct.widget;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

    //
    // POINT TO NOTE : Since we are doing a 'collection'-widget,
    // we need a RemoteViewsService and RemoteViewsService.RemoteViewsFactory classes.
    //
    // at AndroidManifest.xml add :
    // <service android:name=".widget.WidgetService"
    //             android:permission="android:permission.BIND_REMOTEVIEWS"/>
    //
public class WidgetService extends RemoteViewsService {

    private static final String LOG_TAG = WidgetService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) { // 'intent' is passed into the RemoteViewsFactory

        RemoteViewsFactory rfctry = new WidgetDataProvider(this, intent);

        return rfctry;
    }
}
