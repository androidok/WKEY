package com.droider.wkey;

import android.net.wifi.ScanResult;
import android.text.TextUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class mkQuerypwd {

    public String getpwd() throws Exception{
        String salt = "LQ9$ne@gH*Jq%KOL";
        Map<String, String> map = new TreeMap<>();
        map.put("och", "wandoujia");
        map.put("ii", "");
        map.put("appid", "0001");
        map.put("pid", "qryapwd:commonswitch");
        map.put("lang", "cn");
        map.put("v", "58");
        map.put("uhid", "a0000000000000000000000000000001");
        map.put("method", "getDeepSecChkSwitch");
        map.put("st", "m");
        map.put("chanid", "guanwang");
        map.put("sign", "");
        map.put("bssid", "");
        map.put("ssid", "");
        map.put("dhid", this.getdhid());
        map.put("mac", "d8:86:e6:6f:a8:7c");
        StringBuilder bssid = new StringBuilder();
        StringBuilder ssid = new StringBuilder();
        for (ScanResult i : WifiStatus.wifiList){
            bssid.append(i.BSSID).append(",");
            ssid.append(i.SSID).append(",");
        }
        map.put("bssid", bssid.toString());
        map.put("ssid", ssid.toString());
        map.put("sign", getSign(map, salt));

        String response = sendRequest(map);
        JSONObject jsonObject = new JSONObject(response);
        JSONObject psws = jsonObject.getJSONObject("qryapwd").getJSONObject("psws");
        Iterator<String> iterator = psws.keys();
        StringBuilder result = new StringBuilder();
        while (iterator.hasNext()){
            String item = iterator.next();
            String encryptpwd = psws.getJSONObject(item).getString("pwd");
            String decryptpwd = decrypt(encryptpwd);
            int pwdLength = Integer.parseInt(decryptpwd.substring(0, 3));
            String pwd = decryptpwd.substring(3, decryptpwd.length()-1).substring(0, pwdLength);
            String name = psws.getJSONObject(item).getString("ssid");
            result.append("BSSID: ").append(item).append("\nSSID: ").append(name).append("\n密码: ").append(pwd).append("\n\n");
        }
        if (TextUtils.isEmpty(result.toString())){
            return "没找到密码 ¯\\_(ツ)_/¯";
        }
        return result.toString();
    }

    public String getdhid() throws Exception {
        String salt = "LQ9$ne@gH*Jq%KOL";
        Map<String, String> map = new TreeMap<>();
        map.put("capbssid", "d8:86:e6:6f:a8:7c");
        map.put("model", "Nexus+4");
        map.put("och", "wandoujia");
        map.put("appid", "0001");
        map.put("mac", "d8:86:e6:6f:a8:7c");
        map.put("wkver", "2.9.38");
        map.put("lang", "cn");
        map.put("capbssid", "test");
        map.put("uhid", "");
        map.put("st", "m");
        map.put("chanid", "guanwang");
        map.put("dhid", "");
        map.put("os", "android");
        map.put("scrs", "768");
        map.put("imei", "355136052333516");
        map.put("manuf", "LGE");
        map.put("osvercd", "19");
        map.put("ii", "355136052391516");
        map.put("osver", "5.0.2");
        map.put("pid", "initdev:commonswitch");
        map.put("misc", "google/occam/mako:4.4.4/KTU84P/1227136:user/release-keys");
        map.put("sign", "");
        map.put("v", "58");
        map.put("sim", "");
        map.put("method", "getTouristSwitch");
        map.put("scrl", "1184");
        map.put("sign", getSign(map, salt));

        String dhid;
        String response = sendRequest(map);
        JSONObject jsonObject = new JSONObject(response);
        dhid = jsonObject.getJSONObject("initdev").getString("dhid");
        return dhid;
    }

    public String getSign(Map map, String salt) throws Exception {
        String value = "";
        for (Object o : map.entrySet()) {
            Map.Entry<String, String> entry = (Map.Entry) o;
            value += entry.getValue();
        }
        value += salt;
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] digest = messageDigest.digest(value.getBytes("UTF-8"));
        BigInteger number = new BigInteger(1, digest);
        String md5 = number.toString(16);
        while (md5.length() < 32) {
            md5 = "0" + md5;
        }
        return md5.toUpperCase();
    }

    public String getRequestData(Map params) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object o : params.entrySet()) {
            Map.Entry<String, String> entry = (Map.Entry) o;
            stringBuilder.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    public String sendRequest(Map params) throws Exception{
        URL url = new URL("http://wifiapi02.51y5.net/wifiapi/fa.cmd");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setInstanceFollowRedirects(true);
        httpURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        httpURLConnection.setRequestProperty("Host", "wifiapi02.51y5.net");
        httpURLConnection.setRequestProperty("Accept", "text/plain");
        httpURLConnection.connect();

        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream()));
        bufferedWriter.write(getRequestData(params));
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
        return response.toString();
    }

    public String decrypt(String encryptpwd) throws Exception{
        String key = "jh16@`~78vLsvpos";
        String iv = "j#bd0@vp0sj!3jnv";
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        return new String(cipher.doFinal(hex2byte(encryptpwd)));
    }

    public byte[] hex2byte(String hex){
        byte[] output = new byte[hex.length()/2];
        for (int i = 0,j = 0; i < hex.length(); i += 2, j++)
        {
            String str = hex.substring(i, i + 2);
            output[j] = (byte) Integer.parseInt(str, 16);
        }
        return output;
    }

}
