package com.chenzhi.tinkerdemo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.tinker.lib.tinker.TinkerInstaller;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView mTvBug = (TextView) findViewById(R.id.tv_bug);

        mTvBug.setText("现在是报错状态了");

        Toast.makeText(this, "现在是报错状态了", Toast.LENGTH_SHORT).show();

        //进行补丁的操作，暂时用本地代替
        TinkerInstaller.onReceiveUpgradePatch(this,
                Environment.getExternalStorageDirectory().getAbsolutePath()+"/ysb/patch_signed_7zip.apk");
    }
}
