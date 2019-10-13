package br.com.sm.smallpicture;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;

import br.com.sm.smallpicture.util.IabHelper;

/**
 * Created by Luiz Paulo Oliveira on 14/01/2017.
 */

public class MyApplication extends Application {

    private IabHelper mHelper;
    public SharedPreferences settings;
    public boolean premium = false;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {

        super.onCreate();

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        premium = settings.getBoolean("premium", false);
    }

    public IabHelper getmHelper() {
        return mHelper;
    }

    public void setmHelper(IabHelper mHelper) {
        this.mHelper = mHelper;
    }

}
