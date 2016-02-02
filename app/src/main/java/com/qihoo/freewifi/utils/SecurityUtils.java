package com.qihoo.freewifi.utils;

import android.content.Context;

public class SecurityUtils {

    static {
        System.loadLibrary("security");
    }

    public static synchronized native String getKey(String arg1, String arg2) ;

    public static synchronized native String initnew(Context arg1, String arg2, String arg3, boolean arg4);

    public static synchronized native String sign(Context arg1, String arg2, String arg3, boolean arg4);

}