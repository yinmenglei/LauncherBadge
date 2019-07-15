package badge.gaodun.com.launcherbadge;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Function:
 * Author Name: yinmenglei
 * Date: 2019/6/26 14:47
 * Copyright © 2006-2018 高顿网校, All Rights Reserved.
 */
public class BadgeUtil {

    private BadgeUtil() throws InstantiationException {
        throw new InstantiationException("This class is not for instantiation");
    }

    /**
     * 设置Badge 目前支持Launcher
     */
    public static void setBadgeCount(Context context, int count, int iconResId) {
        if (count <= 0) {
            count = 0;
        } else {
            count = Math.max(0, Math.min(count, 99));
        }

        String phoneType = Build.MANUFACTURER.toLowerCase();
        if (phoneType.equalsIgnoreCase("xiaomi")) {
            setBadgeOfMIUI(context, count, iconResId);
        } else if (phoneType.equalsIgnoreCase("sony")) {
            setBadgeOfSony(context, count);
        } else if (phoneType.contains("samsung") || phoneType.contains("lg")) {
            setBadgeOfSumsung(context, count);
        } else if (phoneType.contains("htc")) {
            setBadgeOfHTC(context, count);
        } else if (phoneType.contains("nova")) {
            setBadgeOfNova(context, count);
        } else if (phoneType.contains("oppo")) {
            setBadgeOfOPPO(context, count);
        } else if (phoneType.contains("vivo")) {
            setBadgeOfVIVO(context, count);
        } else if (phoneType.contains("huawei") || Build.BRAND.equals("Huawei") || Build.BRAND.equals("HONOR")) {//华为
            setBadgeOfHuawei(context, count);
        } else if (phoneType.contains("google")) {
            setBadgeOfGoogle(context, count);
        } else {
            Toast.makeText(context, "Not Found Support Launcher:"+phoneType, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 设置MIUI的Badge
     */
    private static void setBadgeOfMIUI(Context context, int count, int iconResId) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            return;
        }
        String notificationChannelId = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = Util.getInstance().createNotificationChannel();
            nm.createNotificationChannel(notificationChannel);
            notificationChannelId = notificationChannel.getId();
        }
        try {
            Notification notification = new NotificationCompat.Builder(context, notificationChannelId)
                    .setSmallIcon(context.getApplicationInfo().icon)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("title")
                    .setContentText("content num: " + count)
                    .setTicker("ticker")
                    .setAutoCancel(true)
                    .setNumber(count)
                    .build();

            Field field = notification.getClass().getDeclaredField("extraNotification");
            Object extraNotification = field.get(notification);
            Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
            method.invoke(extraNotification, count);
            nm.notify(32154, notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置索尼的Badge
     */
    private static void setBadgeOfSony(Context context, int count) {
        String launcherClassName = Util.getInstance().getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        boolean isShow = true;
        if (count == 0) {
            isShow = false;
        }
        Intent localIntent = new Intent();
        localIntent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", isShow);//是否显示
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", launcherClassName);//启动页
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", String.valueOf(count));//数字
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context.getPackageName());//包名
        context.sendBroadcast(localIntent);
    }

    /**
     * 设置三星的Badge\设置LG的Badge
     */
    private static void setBadgeOfSumsung(Context context, int count) {
        String launcherClassName = Util.getInstance().getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);

        if (Util.getInstance().canResolveBroadcast(context, intent)) {
            context.sendBroadcast(intent);
        }
    }

    /**
     * 设置HTC的Badge
     */
    private static void setBadgeOfHTC(Context context, int count) {
        Intent intentNotification = new Intent("com.htc.launcher.action.SET_NOTIFICATION");
        ComponentName localComponentName = new ComponentName(context.getPackageName(), Util.getInstance().getLauncherClassName(context));
        intentNotification.putExtra("com.htc.launcher.extra.COMPONENT", localComponentName.flattenToShortString());
        intentNotification.putExtra("com.htc.launcher.extra.COUNT", count);
        context.sendBroadcast(intentNotification);

        Intent intentShortcut = new Intent("com.htc.launcher.action.UPDATE_SHORTCUT");
        intentShortcut.putExtra("packagename", context.getPackageName());
        intentShortcut.putExtra("count", count);
        context.sendBroadcast(intentShortcut);
    }

    /**
     * 设置Nova的Badge
     */
    private static void setBadgeOfNova(Context context, int count) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("tag", context.getPackageName() + "/" + Util.getInstance().getLauncherClassName(context));
        contentValues.put("count", count);
        context.getContentResolver().insert(Uri.parse("content://com.teslacoilsw.notifier/unread_count"), contentValues);
    }

    /**
     * 设置vivo的Badge :vivoXplay5 vivo x7无效果
     */
    private static void setBadgeOfVIVO(Context context, int count) {
        try {
            Intent intent = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
            intent.putExtra("packageName", context.getPackageName());
            intent.putExtra("className", Util.getInstance().getLaunchIntentForPackage(context));
            intent.putExtra("notificationNum", count);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置oppo的Badge :oppo角标提醒目前只针对内部软件还有微信、QQ开放，其他的暂时无法提供
     */
    private static void setBadgeOfOPPO(Context context, int count) {
        try {
            Bundle extras = new Bundle();
            extras.putInt("app_badge_count", count);
            context.getContentResolver().call(Uri.parse("content://com.android.badge/badge"),
                    "setAppBadgeCount", String.valueOf(count), extras);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置华为的Badge :mate8 和华为 p7,honor畅玩系列可以,honor6plus 无效果
     */
    private static void setBadgeOfHuawei(Context context, int count) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("package", context.getPackageName());
            bundle.putString("class", Util.getInstance().getLaunchIntentForPackage(context));
            bundle.putInt("badgenumber", count);
            context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"),
                    "change_badge", null, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setBadgeOfMadMode(Context context, int count, String packageName, String className) {
        try {
            Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
            intent.putExtra("badge_count", count);
            intent.putExtra("badge_count_package_name", packageName);
            intent.putExtra("badge_count_class_name", className);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // google
    private static void setBadgeOfGoogle(Context context, int count) {
        try {
            Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
            intent.putExtra("badge_count", count);
            intent.putExtra("badge_count_package_name", context.getPackageName());
            intent.putExtra("badge_count_class_name", Util.getInstance().getLaunchIntentForPackage(context));
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 重置Badge
     */
    public static void resetBadgeCount(Context context, int iconResId) {
        setBadgeCount(context, 0, iconResId);
    }

}
