package tv.fengmang.ccpush.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;


public class DmpNetUtils {

    private final static String TAG = "DmpNetUtils";

    /**
     * 更多无效的mac
     */
    private static final String[] NOUSE_MAC_ARRAY = {
            "00:00:00:00:00:00",
            "01:00:00:00:00:00",
            "02:00:00:00:00:00",
            "04:00:00:00:00:00",
            "FF:FF:FF:FF:FF:FF",
            "12:34:56:78:90:12",
            "12:34:56:78:90:AB",
            "88:88:88:88:88:88",
            "52:54:00:12:34:56"
    };

    private final static String NOUSE_MAC = "02:00:00:00:00:00";

    private final static String FILE_NAME = "device_mac_address_config";
    private final static String KEY_NAME = "MAC";


    /**
     * 获取mac地址
     * 获取mac经典场景
     * 1 apk权限不足，或者系统限制导致无法获取到 /sys/class/net/eth0/address /sys/class/net/wlan0/address文件记录的mac
     * 2 盒子插有线时能读到/sys/class/net/eth0/address 连wifi时只能读到/sys/class/net/wlan0/address里的数据
     * 3 读系统文件不到时，通过有线连接的ip或者wifi连接的ip转mac
     * 4 都没有（没网络时,或者没权限），取一个UUID 存起来
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getMacAddress(Context context) {
        // 都取不到，读存储的mac
        String macAddress = getData(context, KEY_NAME);
        if (!isNoUseMac(macAddress)) {
            return macAddress;
        }

        //优先读系统有线mac 无线mac
        macAddress = getEthMac(context);
        if (!isNoUseMac(macAddress)) {
            Log.d(TAG, "system eth0 mac ok");
            setData(context, KEY_NAME, macAddress);
            return macAddress;
        }

        //读系统无线mac
        macAddress = getWifiMac(context);
        if (!isNoUseMac(macAddress)) {
            Log.d(TAG, "system wifi mac ok");
            setData(context, KEY_NAME, macAddress);
            return macAddress;
        }

        //有线ip 转mac并存SharedPreferences
        macAddress = getMacEthReal();
        if (!isNoUseMac(macAddress)) {
            Log.d(TAG, "network eth0 mac ok");
            setData(context, KEY_NAME, macAddress);
            return macAddress;
        }

        //无线ip 转mac并存SharedPreferences
        macAddress = getWifiMacReal(context);
        if (!isNoUseMac(macAddress)) {
            Log.d(TAG, "network wifi mac ok");
            setData(context, KEY_NAME, macAddress);
            return macAddress;
        }

        //uuid 转mac并存SharedPreferences
        macAddress = getUUID();
        Log.d(TAG, "uuid generate mac ok");
        setData(context, KEY_NAME, macAddress);
        return macAddress;
    }


    public static String getEthMac(Context context) {
        String macAddress = null;
        macAddress = getEthMacByCmd();
        if (isNoUseMac(macAddress)) {
            macAddress = getFileInfo("/sys/class/net/eth0/address");
        } else {
            return macAddress;
        }
        if (isNoUseMac(macAddress)) {
            Log.e(TAG, "get eth0 mac failed,maybe need system auth");
        }
        macAddress = getMacFromHardware("eth0");
        if (isNoUseMac(macAddress)) {
            Log.e(TAG, "get eth0 mac from hardware failed!");
        }
        return macAddress;
    }

    public static String getWifiMac(Context context) {
        String macAddress = null;
        macAddress = getWlanMacByCmd();
        if (isNoUseMac(macAddress)) {
            macAddress = getFileInfo("/sys/class/net/wlan0/address");
        } else {
            return macAddress;
        }
        if (isNoUseMac(macAddress)) {
            Log.e(TAG, "get wifi mac from file failed");
        }
        macAddress = getMacFromHardware("wlan0");
        if (isNoUseMac(macAddress)) {
            Log.e(TAG, "get wlan0 mac from hardware failed!");
        }
        return macAddress;
    }

    private static String getEthMacByCmd() {
        String cmd = "cat /sys/class/net/eth0/address";
        ShellUtil.CommandResult res = ShellUtil.execCommand(cmd, false);
        if (res.result == 0)
            return res.successMsg;
        else
            return NOUSE_MAC;
    }

    private static String getWlanMacByCmd() {
        String cmd = "cat /sys/class/net/wlan0/address";
        ShellUtil.CommandResult res = ShellUtil.execCommand(cmd, false);
        if (res.result == 0)
            return res.successMsg;
        else
            return NOUSE_MAC;
    }


    private static String getMacEthReal() {
        String macEth = null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> net = intf.getInetAddresses(); net
                        .hasMoreElements(); ) {
                    InetAddress iaddr = net.nextElement();
                    if (iaddr instanceof Inet4Address) {
                        if (!iaddr.isLoopbackAddress()) {
                            byte[] data = intf.getHardwareAddress();
                            macEth = getAddressFromByte(data);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return macEth;
    }

    @SuppressLint("MissingPermission")
    private static String getWifiMacReal(Context context) {
        String wifiMac = null;
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wm != null || wm.getConfiguredNetworks() != null) {
            wifiMac = wm.getConnectionInfo().getMacAddress();
        }
        return wifiMac;
    }


    public static String getMacFromHardware(String networkInterfaceName) {
        String mac = null;
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    continue;
                }

                StringBuilder sb = new StringBuilder();
                for (byte b : macBytes) {
                    sb.append(String.format("%02X:", b));
                }

                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                Log.d(TAG, "NetworkInterface:" + nif.getName() + ", " + sb.toString());
                if (nif.getName().equalsIgnoreCase(networkInterfaceName)) {
                    mac = sb.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mac;
    }


    /**
     * 得到全局唯一UUID
     */
    private static String getUUID() {
        String uuid = UUID.randomUUID().toString().replaceAll("\\-", "");
        return uuid;
    }

    private static String getFileInfo(String filename) {
        if (filename == null || filename.length() < 1) {
            return null;
        }
        File file = new File(filename);
        if (!file.isFile()) {
            return null;
        }
        String info = null;
        FileReader fr = null;
        try {
            fr = new FileReader(filename);
            StringBuilder sb = new StringBuilder();
            int ch = 0;
            while ((ch = fr.read()) != -1) {
                if ('\n' == ch || '\r' == ch) {
                    continue;
                }
                sb.append((char) ch);
            }
            info = sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "error", e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception e) {
                }
            }
        }

        Log.d(TAG, "info:" + info);
        return info;
    }

    private static String getAddressFromByte(byte[] address) {
        if (address == null || address.length != 6) {
            return null;
        }
        return String.format("%02X:%02X:%02X:%02X:%02X:%02X",
                address[0], address[1], address[2], address[3], address[4], address[5]);
    }


    private static boolean isNoUseMac(String mac) {
        if (TextUtils.isEmpty(mac)) {
            return true;
        }
        for (String m : NOUSE_MAC_ARRAY) {
            if (m.equalsIgnoreCase(mac)) {
                return true;
            }
        }
        return false;
    }

    private static String getData(Context context, String key) {
        //1、获取Preferences
        SharedPreferences settings = context.getSharedPreferences(FILE_NAME, 0);
        //2、取出数据
        String name = settings.getString(key, "");
        //Log.d("test", "name:" + name);
        return name;
    }

    private static void setData(Context context, String key, String value) {
        //1、打开Preferences，名称为setting，如果存在则打开它，否则创建新的Preferences
        SharedPreferences settings = context.getSharedPreferences(FILE_NAME, 0);
        //2、让setting处于编辑状态
        SharedPreferences.Editor editor = settings.edit();
        //3、存放数据
        editor.putString(key, value);
        //4、完成提交
        editor.commit();
        Log.d(TAG, "set value:" + value);
    }

}
