package com.droider.wkey;

import android.app.AlertDialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    Context mContext = this;
    TextView textView;
    WifiStatus wifiStatus;
    Handler mHandler;
    final int UPDATE_TEXT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("WIFI密码查询工具");
        textView = (TextView) findViewById(R.id.content);
        wifiStatus = new WifiStatus(mContext);
        mHandler = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_TEXT:
                        textView.setText(msg.obj.toString());
                        break;
                }
            }
        };
        showWifiStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                showWifiStatus();
                break;
            case R.id.action_mkquery:
                try {
                    Toast.makeText(mContext,"查询中...",Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            try {
                                Message msg = new Message();
                                msg.what = UPDATE_TEXT;
                                msg.obj = new mkQuerypwd().getpwd();
                                mHandler.sendMessage(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_qhquery:
                try {
                    Toast.makeText(mContext,"查询中...",Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            try {
                                Message msg = new Message();
                                msg.what = UPDATE_TEXT;
                                msg.obj = new qhQuerypwd(MainActivity.this).getpwd();
                                mHandler.sendMessage(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_about:
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle("关于");
                alertBuilder.setMessage("这个程序仅仅用于演示它的功能, 必须运行在您有明确的权利来使用的网络上. 任何的其他用法与开发者无关。");
                alertBuilder.setCancelable(false);
                alertBuilder.setPositiveButton("确定", null);
                alertBuilder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showWifiStatus(){
        wifiStatus.scanWifi();
        textView.setText("");
        for (ScanResult i : WifiStatus.wifiList){
            textView.append("BSSID: "+i.BSSID+"\nSSID: "+i.SSID+"\n信号强度: "+i.level+"dbm\n\n");
        }
    }


}
