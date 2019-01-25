package com.bytedance.camera.demo;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.bytedance.camera.demo.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_IMAGE;
import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_VIDEO;
import static com.bytedance.camera.demo.utils.Utils.getOutputMediaFile;

public class CustomCameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;
    private Camera mCamera;

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;

    private int rotationDegree = 0;
    private File imgFile;
    private File videoFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_custom_camera);

        mSurfaceView = findViewById(R.id.img);
        //todo 给SurfaceHolder添加Callback
        SurfaceHolder surfaceHolder =this.mSurfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);

        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            //todo 拍一张照片

            takePicture();//don't know how to do

            /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            imgFile = Utils.getOutputMediaFile(Utils.MEDIA_TYPE_IMAGE);
            if (imgFile != null){
                Uri fileUri = FileProvider.getUriForFile(this, "com.bytedance.camera.demo", this.imgFile);
                //getUriForFile里面第二个参数Authority根据manifest里面的Authority来写
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(takePictureIntent, 101);

            }*/
        });

        prepareVideoRecorder();
        findViewById(R.id.btn_record).setOnClickListener(v -> {
            //todo 录制，第一次点击是start，第二次点击是stop
            takeVideo();
            //startPreview(surfaceHolder);
            if (isRecording) {
                //todo 停止录制
                releaseMediaRecorder();
                isRecording = false;
            } else if (prepareVideoRecorder()){
                //todo 录制
                mMediaRecorder.start();
                //startPreview(surfaceHolder);
                isRecording = true;
                }else{
                releaseMediaRecorder();
            }

        });

        findViewById(R.id.btn_facing).setOnClickListener(v -> {
            //todo 切换前后摄像头
            if (CAMERA_TYPE == 0) {
                mCamera =getCamera(1);
            } else {
                mCamera = getCamera(0);
            }
            startPreview(mSurfaceView.getHolder());
            /*switch (CAMERA_TYPE )
            {
                case 0:{
                    mCamera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    CAMERA_TYPE = 1;
                    break;
                }
                case 1:{
                    mCamera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    CAMERA_TYPE = 0;
                    break;
                }
            }*/



        });

        findViewById(R.id.btn_zoom).setOnClickListener(v -> {
            //todo 调焦，需要判断手机是否支持
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.isZoomSupported()) {
                try {
                    parameters.setZoom(parameters.getZoom() + 1);
                    mCamera.setParameters(parameters);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void takeVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if(takeVideoIntent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takeVideoIntent,101);
        }
    }
    private void takePicture() {
        //todo 打开相机
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imgFile = Utils.getOutputMediaFile(Utils.MEDIA_TYPE_IMAGE);
        if (imgFile != null){
            Uri fileUri = FileProvider.getUriForFile(this, "com.bytedance.camera.demo", this.imgFile);
            //getUriForFile里面第二个参数Authority根据manifest里面的Authority来写
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(takePictureIntent, 101);
        }
    }

    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);
        //todo 摄像头添加属性，例是否自动对焦，设置旋转方向等
        this.rotationDegree = getCameraDisplayOrientation(position);
        cam.setDisplayOrientation(this.rotationDegree);
        Camera.Parameters params = cam.getParameters();
        if (params.getSupportedFocusModes().contains("continuous-picture")) {
            params.setFocusMode("continuous-picture");
            cam.setParameters(params);
        }

        cam.setDisplayOrientation(getCameraDisplayOrientation(CAMERA_TYPE));//ID??!!!!!
        return cam;
    }


    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        //todo 释放camera资源
        mCamera.stopPreview();
        mCamera.release();
        mCamera.lock();
        mCamera=null;
    }

    Camera.Size size;

    private void startPreview(SurfaceHolder holder) {
        //todo 开始预览
        //getOptimalPreviewSize(  );
        //startPreview(holder);
        if (this.mCamera == null) {
            this.mCamera = getCamera(this.CAMERA_TYPE);
        }
        try {
            this.size = getOptimalPreviewSize(this.mCamera.getParameters().getSupportedPreviewSizes(), this.mSurfaceView.getWidth(), this.mSurfaceView.getHeight());
            this.mCamera.getParameters().setPreviewSize(this.size.width, this.size.height);
            this.mCamera.setPreviewDisplay(holder);
            this.mCamera.startPreview();
            this.mCamera.cancelAutoFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private MediaRecorder mMediaRecorder;

    private boolean prepareVideoRecorder() {
        //todo 准备MediaRecorder
        this.mCamera.unlock();
        this.mMediaRecorder = new MediaRecorder();

        this.mMediaRecorder.setCamera(this.mCamera);
        this.mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//1

        this.mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        this.mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        this.mMediaRecorder.setPreviewDisplay(this.mSurfaceView.getHolder().getSurface());
        this.mMediaRecorder.setOrientationHint(this.rotationDegree);

        try {
            this.mMediaRecorder.prepare();
            return true;
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e2) {
            releaseMediaRecorder();
            return false;
        }

    }


    private void releaseMediaRecorder() {
        //todo 释放MediaRecorder
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        mCamera.lock();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //todo 释放Camera和MediaRecorder资源
        releaseCameraAndPreview();
        releaseMediaRecorder();
    }


    private Camera.PictureCallback mPicture = (data, camera) -> {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            Log.d("mPicture", "Error accessing file: " + e.getMessage());
        }

        mCamera.startPreview();
    };


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}
