package com.bytedance.camera.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
import android.widget.ImageView;

import com.bytedance.camera.demo.utils.Utils;

import java.io.File;

public class TakePictureActivity extends AppCompatActivity {

    private ImageView imageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

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
        setContentView(R.layout.activity_take_picture);
        imageView = findViewById(R.id.img);
        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(TakePictureActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(TakePictureActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //todo 在这里申请相机、存储的权限
                        ActivityCompat.requestPermissions(TakePictureActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);

            } else {
                takePicture();
            }
        });

    }

    private  File imgFile;
    private void takePicture() {
        //todo 打开相机
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imgFile = Utils.getOutputMediaFile(Utils.MEDIA_TYPE_IMAGE);
        if (imgFile != null){
            Uri fileUri = FileProvider.getUriForFile(this, "com.bytedance.camera.demo", this.imgFile);
            //getUriForFile里面第二个参数Authority根据manifest里面的Authority来写
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic();
        }
    }

    private void setPic() {
        //todo 根据imageView裁剪

        //todo 根据缩放比例读取文件，生成Bitmap

        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();
        //Get the dimension of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgFile.getAbsolutePath(), bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        //Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        //Decode the image file into a Bitmap sized to fill the view
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath(),bmOptions);
        imageView.setImageBitmap(bmp);

        //todo 如果存在预览方向改变，进行图片旋转

        //todo 如果存在预览方向改变，进行图片旋转

        Utils.rotateImage(bmp, imgFile.getAbsolutePath() );

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                //todo 判断权限是否已经授予
                for (int i = 0; i < permissions.length ; i++ ){
                    Log.i("MainActivity","申请的权限为：" + permissions[i] +"，申请结果：" +
                    grantResults[i]);
                }
                if (grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED
                        && grantResults[1] == getPackageManager().PERMISSION_GRANTED )
                    takePicture();
                break;
            }
        }
    }
}
