package com.voler.paydemo;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class BabyApplication extends Application{



    private String WX_APP_ID = "";

    public static final String TAG = BabyApplication.class.getSimpleName();
    public static BabyApplication instance;

    public static BabyApplication getInstance(){
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }


    /**
     * 返回当前程序版本名
     */
    public final static String getAppVersionName() {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = getInstance().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getInstance().getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {

        }
        return versionName;
    }


    /**
     * 获取当前程序build
     */
    public final static int getAppVersionCode() {
        int versionCode = 1;
        try {
            // ---get the package info---
            PackageManager pm = getInstance().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getInstance().getPackageName(), 0);
            versionCode = pi.versionCode;

        } catch (Exception e) {

        }
        return versionCode;
    }



    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // 低内存情况下调用Java垃圾回收器
        System.gc();
    }

    private List<Activity> activities = new ArrayList<Activity>();

    public void addActivity(Activity activity) {
        activities.add(activity);
    }
    public void removeActivity(Activity activity){
        activities.remove(activity);
    }

    public FragmentActivity getMainActivity(){
        for (int i = 0 ; i < activities.size() ; i++){
            if(activities.get(i) instanceof  MainActivity){
                return (FragmentActivity) activities.get(i);
            }
        }
        return null;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        for (Activity activity : activities) {
            activity.finish();
        }

        System.exit(0);
    }

}
