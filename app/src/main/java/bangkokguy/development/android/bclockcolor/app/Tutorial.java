package bangkokguy.development.android.bclockcolor.app;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.TextView;

import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Tutorial implements IXposedHookLoadPackage {

    String text;
    String ssid;
    WifiInfo wifiInfo;
    WifiManager wifiManager;
    TextView tv;
    MyHook myHook;

    private final static boolean DEBUG=false;
    private final static String TAG="bclockcolor ";

    public class MyHook extends XC_MethodHook {

        void wifi_enabled () {

            wifiInfo = wifiManager.getConnectionInfo();
            if(DEBUG)XposedBridge.log(TAG+"wifi enabled. wifiInfo" + wifiInfo.toString());
            if (wifiInfo.getNetworkId()==-1) { //no network connected
                if(DEBUG)XposedBridge.log(TAG+"no network connected");
                tv.setText("--" + text);
                tv.setTextColor(Color.RED);
            } else {//network connected
                ssid = wifiInfo.getSSID();
                if (ssid == null) {
                    ssid = "null";
                    if(DEBUG)XposedBridge.log(TAG + "ssid==null");
                    tv.setTextColor(Color.RED);
                } else {
                    if (ssid.length() > 6) ssid = ssid.substring(0, 6);
                    if (ssid != "") ssid = ssid + ":";
                    else ssid = "fasz";
                    tv.setText(ssid + text);
                    tv.setTextColor(Color.GREEN);
                }
            }
        }

        void wifi_disabled () {
            tv.setText(text);
            tv.setTextColor(Color.CYAN);
        }

        @Override
        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            // this will be called after the clock was updated by the original method
            if(DEBUG)XposedBridge.log(TAG+"***---: " + "afterHookedMethod");

            Context mContext = (Context) getObjectField(param.thisObject, "mContext");
            tv = (TextView) param.thisObject;
            text = tv.getText().toString();

            wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

            if (wifiManager.isWifiEnabled ()) //wifi enabled
                wifi_enabled();
            else
                wifi_disabled ();
        }
    }

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if(DEBUG)XposedBridge.log(TAG+"***---Loaded app: " + lpparam.packageName);
        if (!lpparam.packageName.equals("com.android.systemui"))
            return;

        myHook = new MyHook();
        findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock", myHook);

    //protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    // this will be called before the clock was updated by the original method
    //}
    }
}