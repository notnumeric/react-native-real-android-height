
package com.ranna.arh;

import java.lang.Math;
import java.lang.reflect.InvocationTargetException;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.provider.Settings;
import android.content.res.Resources;
import android.view.WindowManager;
import android.view.ViewConfiguration;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

import java.util.HashMap;
import java.util.Map;

import java.lang.reflect.Field;

public class RNAndroidRealHeightModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

  private final ReactApplicationContext mReactContext;

  public RNAndroidRealHeightModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.mReactContext = reactContext;
    mReactContext.addLifecycleEventListener(this);
  }

  @Override
  public String getName() {
    return "RNAndroidRealHeight";
  }

  @Override
  public void onHostDestroy() {

  }

  @Override
  public void onHostPause() {

  }

  @Override
  public void onHostResume() {

  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants =  new HashMap<>();

    final Context ctx = getReactApplicationContext();
    final DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();

    // Get the real display metrics if we are using API level 17 or higher.
    // The real metrics include system decor elements (e.g. soft menu bar).
    //
    // See: http://developer.android.com/reference/android/view/Display.html#getRealMetrics(android.util.DisplayMetrics)
    if (Build.VERSION.SDK_INT >= 17) {
      Display display = ((WindowManager) mReactContext.getSystemService(Context.WINDOW_SERVICE))
              .getDefaultDisplay();
      try {
        Display.class.getMethod("getRealMetrics", DisplayMetrics.class).invoke(display, metrics);
      } catch (InvocationTargetException e) {
      } catch (IllegalAccessException e) {
      } catch (NoSuchMethodException e) {
      }
    }

    constants.put("REAL_WINDOW_HEIGHT", getRealHeight(metrics));
    constants.put("REAL_WINDOW_WIDTH", getRealWidth(metrics));
    constants.put("STATUS_BAR_HEIGHT", getStatusBarHeight(metrics));
    constants.put("SOFT_MENU_BAR_HEIGHT", getSoftMenuBarHeight(metrics));
    constants.put("SMART_BAR_HEIGHT", getSmartBarHeight(metrics));
    constants.put("SOFT_MENU_BAR_ENABLED", hasPermanentMenuKey());

    return constants;
  }

  private boolean hasPermanentMenuKey() {
    final Context ctx = getReactApplicationContext();
    int id = ctx.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
    return !(id > 0 && ctx.getResources().getBoolean(id));
  }

  private float getStatusBarHeight(DisplayMetrics metrics) {
    final Context ctx = getReactApplicationContext();
    final int heightResId = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
    return
            heightResId > 0
                    ? ctx.getResources().getDimensionPixelSize(heightResId) / metrics.density
                    : 0;
  }

  private float getSoftMenuBarHeight(DisplayMetrics metrics) {
    if(hasPermanentMenuKey()) {
      return 0;
    }
    final Context ctx = getReactApplicationContext();
    final int heightResId = ctx.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
    return
            heightResId > 0
                    ? ctx.getResources().getDimensionPixelSize(heightResId) / metrics.density
                    : 0;
  }

  private float getRealHeight(DisplayMetrics metrics) {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    Display display = getCurrentActivity().getWindowManager().getDefaultDisplay();
    Point size = new Point();
    Point realSize = new Point();
    display.getSize(size);
    display.getRealSize(realSize);
    int w = size.x;
    int h = size.y;
    int rw = realSize.x;
    int rh = realSize.y;

    float status_bar = getStatusBarHeight(displayMetrics);
    float soft_menu = getSoftMenuBarHeight(displayMetrics);
    float smart_bar = getSmartBarHeight(displayMetrics);

    Point nsize = getNavigationBarSize(getReactApplicationContext());
    float nav_bar = nsize.y / displayMetrics.density;

    float height = displayMetrics.heightPixels / displayMetrics.density;
    float real_height = rh / displayMetrics.density;
    float hh = real_height - status_bar - soft_menu - smart_bar - nav_bar;

    return hh;
  }

  public static Point getNavigationBarSize(Context context) {
    Point appUsableSize = getAppUsableScreenSize(context);
    Point realScreenSize = getRealScreenSize(context);

    // navigation bar on the side
    if (appUsableSize.x < realScreenSize.x) {
      return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
    }

    // navigation bar at the bottom
    if (appUsableSize.y < realScreenSize.y) {
      return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
    }

    // navigation bar is not present
    return new Point();
  }

  public static Point getAppUsableScreenSize(Context context) {
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    return size;
  }

  public static Point getRealScreenSize(Context context) {
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();
    Point size = new Point();

    if (Build.VERSION.SDK_INT >= 17) {
      display.getRealSize(size);
    } else if (Build.VERSION.SDK_INT >= 14) {
      try {
        size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
        size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
      } catch (IllegalAccessException e) {} catch (InvocationTargetException e) {} catch (NoSuchMethodException e) {}
    }

    return size;
  }

  private float getRealWidth(DisplayMetrics metrics) {
    return metrics.widthPixels / metrics.density;
  }

  private float getSmartBarHeight(DisplayMetrics metrics) {
    final Context context = getReactApplicationContext();
    final boolean isMeiZu = Build.MANUFACTURER.equals("Meizu");

    final boolean autoHideSmartBar = Settings.System.getInt(context.getContentResolver(),
            "mz_smartbar_auto_hide", 0) == 1;

    if (!isMeiZu || autoHideSmartBar) {
      return 0;
    }
    try {
      Class c = Class.forName("com.android.internal.R$dimen");
      Object obj = c.newInstance();
      Field field = c.getField("mz_action_button_min_height");
      int height = Integer.parseInt(field.get(obj).toString());
      return context.getResources().getDimensionPixelSize(height) / metrics.density;
    } catch (Throwable e) { // 不自动隐藏smartbar同时又没有smartbar高度字段供访问，取系统navigationbar的高度
      return getNormalNavigationBarHeight(context) / metrics.density;
    }
    //return getNormalNavigationBarHeight(context) / metrics.density;
  }

  protected static float getNormalNavigationBarHeight(final Context ctx) {
    try {
      final Resources res = ctx.getResources();
      int rid = res.getIdentifier("config_showNavigationBar", "bool", "android");
      if (rid > 0) {
        boolean flag = res.getBoolean(rid);
        if (flag) {
          int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
          if (resourceId > 0) {
            return res.getDimensionPixelSize(resourceId);
          }
        }
      }
    } catch (Throwable e) {
      return 0;
    }
    return 0;
  }
}