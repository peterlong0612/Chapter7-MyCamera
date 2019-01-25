package com.bytedance.camera.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.VideoView;

import com.bytedance.camera.demo.utils.Utils;

import java.io.File;

public class RecordVideoActivity extends AppCompatActivity {

    private VideoView videoView;
    private static final int REQUEST_VIDEO_CAPTURE = 1;

    private static final int REQUEST_EXTERNAL_CAMERA = 101;

    private static final int REQUEST_EXTERNAL_STORAGE = 101;
    //定义全局变量的权限数组和状态请求码
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video);
        videoView = findViewById(R.id.img);
        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(RecordVideoActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //todo 在这里申请相机、存储的权限

                ActivityCompat.requestPermissions(RecordVideoActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);

            } else {
                //todo 打开相机拍摄

                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(takeVideoIntent,REQUEST_VIDEO_CAPTURE);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            //todo 播放刚才录制的视频
            Uri videoUri = intent.getData();
            videoView.setVideoURI(videoUri);
            videoView.start();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_CAMERA: {
                //todo 判断权限是否已经授予
                for (int i = 0; i < permissions.length ; i++ ){
                    Log.i("MainActivity","申请的权限为：" + permissions[i] +"，申请结果：" +
                            grantResults[i]);
                }
                /*if (grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED
                        && grantResults[1] == getPackageManager().PERMISSION_GRANTED )
                    //takeVideo();*/
                break;
            }
        }
    }
}
