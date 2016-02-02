package com.droider.wkey;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

import com.qihoo.freewifi.utils.SecurityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class qhQuerypwd {

    final String getUserConfig = "User.getConfig";
    final String getPassword = "Wifi.password";
    public Context mContext;
    public String m2;

    public qhQuerypwd(Context context) throws Exception{
        this.mContext = context;
        String deviceId, androidId, serialNo;
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = telephonyManager.getDeviceId();
        androidId = Settings.Secure.getString(this.mContext.getContentResolver(), "android_id");
        Class<?> mClass = Class.forName("android.os.SystemProperties");
        serialNo = (String) mClass.getMethod("get", String.class).invoke(mClass, "ro.serialno");

        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] digest = messageDigest.digest((deviceId + androidId + serialNo).getBytes("UTF-8"));
        BigInteger number = new BigInteger(1, digest);
        String md5 = number.toString(16);
        while (md5.length() < 32) {
            md5 = "0" + md5;
        }
        this.m2 = md5;

    }

    public String getpwd() throws Exception{
        Map<String, String> map = new TreeMap<>();
        map.put("check_update_key","");
        map.put("full","1");
        map.put("qid","0");
        map.put("devtype","android");
        map.put("nettype","WIFI");
        map.put("manufacturer",Build.MANUFACTURER);
        map.put("model",Build.MODEL);
        map.put("os",Build.VERSION.RELEASE);
        map.put("channel","100000");
        map.put("v","332");
        map.put("m2",this.m2);
        map.put("nance",String.valueOf(System.currentTimeMillis()));
        map.put("inviter_qid","0");
        map.put("method",getPassword);
        map.put("lld","");
        map.put("tp", "1");

        String signUrl = getSignUrl(map,getPassword);

        URL url = new URL(signUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setInstanceFollowRedirects(true);
        httpURLConnection.setRequestProperty("User-agent", "360freewifi");
        httpURLConnection.setRequestProperty("Host", "api.free.wifi.360.cn");
        httpURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        httpURLConnection.connect();

        JSONArray jsonArray = new JSONArray();

        for (ScanResult i : WifiStatus.wifiList){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mac", i.BSSID);
            jsonObject.put("ssid",i.SSID);
            jsonObject.put("enc_type",getEncType(i));
            jsonObject.put("lat", "0.0");
            jsonObject.put("lng", "0.0");
            jsonObject.put("alt", "0.0");
            jsonObject.put("signal",100);
            jsonArray.put(jsonObject);
        }

        String encryptparams = encrypt(jsonArray.toString(),getPassword);

        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream()));
        bufferedWriter.write("params=" + Uri.encode(encryptparams));
        bufferedWriter.flush();
        bufferedWriter.close();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String Line;
        while ((Line = bufferedReader.readLine())!=null){
            response.append(Line).append("\n");
        }
        bufferedReader.close();
        httpURLConnection.disconnect();

        String encryptresult = new JSONObject(response.toString()).getJSONObject("data").getString("list");

        JSONArray result = new JSONArray(decrypt(encryptresult,getPassword));
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0;i<result.length();i++){
            JSONObject jsonObject = result.getJSONObject(i);
            String bssid = jsonObject.getString("mac");
            String ssid = jsonObject.getString("ssid");
            String pwd = jsonObject.getString("pwd");
            if (TextUtils.isEmpty(pwd)){
                pwd = "没找到密码 ¯\\_(ツ)_/¯";
            }
            stringBuilder.append("BSSID: ").append(bssid).append("\nSSID: ").append(ssid).append("\n密码: ").append(pwd).append("\n\n");
        }

        return stringBuilder.toString();
    }

    public String getSignUrl(Map params, String method) throws Exception{
        StringBuilder stringBuilder = new StringBuilder();
        for (Object o : params.entrySet()) {
            Map.Entry<String, String> entry = (Map.Entry) o;
            stringBuilder.append(entry.getKey()).append("=").append(Uri.encode(entry.getValue())).append("&");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        String str = stringBuilder.toString();
        String sign;
        switch (method) {
            case getUserConfig:
                sign = SecurityUtils.initnew(this.mContext, str, "", true).toLowerCase();
                return "http://api.free.wifi.360.cn/intf.php?"+str+"&sign="+sign;
            case getPassword:
                sign = SecurityUtils.sign(this.mContext, str, getConfig(), true).toLowerCase();
                return "http://api.free.wifi.360.cn/intf.php?"+str+"&sign="+sign;
            default:
                return "";
        }
    }

    public String getConfig() throws Exception{
        Map<String, String> map = new TreeMap<>();
        map.put("qid","0");
        map.put("devtype","android");
        map.put("nettype","WIFI");
        map.put("manufacturer",Build.MANUFACTURER);
        map.put("model",Build.MODEL);
        map.put("os",Build.VERSION.RELEASE);
        map.put("channel","100000");
        map.put("v","332");
        map.put("m2",this.m2);
        map.put("nance",String.valueOf(System.currentTimeMillis()));
        map.put("inviter_qid","0");
        map.put("method",getUserConfig);
        map.put("lld","");
        map.put("tp", "1");
        String signUrl = getSignUrl(map, getUserConfig);
        URL url = new URL(signUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setInstanceFollowRedirects(true);
        httpURLConnection.setRequestProperty("User-agent", "360freewifi");
        httpURLConnection.setRequestProperty("Host", "api.free.wifi.360.cn");
        httpURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        httpURLConnection.connect();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String Line;
        while ((Line = bufferedReader.readLine())!=null){
            response.append(Line).append("\n");
        }
        bufferedReader.close();
        httpURLConnection.disconnect();

        JSONObject jsonObject = new JSONObject(response.toString());
        String signConfig = jsonObject.getJSONObject("data").getString("url");
        return decrypt(signConfig, getUserConfig);

    }

    public String encrypt(String str, String method) throws Exception{
        String secretKey = SecurityUtils.getKey(method, this.m2);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "DESede");
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] data = cipher.doFinal(str.getBytes());
        return new String(Base64.encode(data, Base64.DEFAULT), "UTF-8");
    }

    public String decrypt(String encryptStr, String method) throws Exception{
        byte[] data = Base64.decode(encryptStr, Base64.DEFAULT);
        String secretKey = SecurityUtils.getKey(method, this.m2);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "DESede");
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return new String(cipher.doFinal(data), "UTF-8");
    }

    public int getEncType(ScanResult i){
        int enctype;
        if(i.capabilities.contains("WEP")) {
            enctype = 1;
        }
        else if(i.capabilities.contains("PSK")) {
            enctype = 2;
        }
        else if(i.capabilities.contains("EAP")) {
            enctype = 3;
        }
        else {
            enctype = 0;
        }
        return enctype;
    }

}
