package com.example.liuyan.mycamera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, android.hardware.Camera.AutoFocusCallback {

    SurfaceView mySurfaceView;
    SurfaceHolder holder;
    android.hardware.Camera myCamera;
    //照片保存路径
    String filePath = null;
    //是否单击标志
    boolean isClick = false;
    //拍照按钮
    Button capture;
    //照片缩略图
    ImageView editPic;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置屏幕方向
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        setContentView(R.layout.activity_main);

        mySurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        holder = mySurfaceView.getHolder();
        //回调
        holder.addCallback(this);
        mContext = this;
        //设置类型，没有这句将调用失败
        //当在Canvas中绘制完成后，调用函数unlockCanvasAndPost(Canvas canvas)来通知系统Surface已经绘制完成，
        // 这样系统会把绘制完的内容显示出来。为了充分利用不同平台的资源，发挥平台的最优效果可以通过SurfaceHolder的setType函数来设置绘制的类型，
        // 目前接收如下的参数：
        //SURFACE_TYPE_NORMAL：用RAM缓存原生数据的普通Surface
        //SURFACE_TYPE_HARDWARE：适用于DMA(Direct memory access )引擎和硬件加速的Surface
        //SURFACE_TYPE_GPU：适用于GPU加速的Surface
        //SURFACE_TYPE_PUSH_BUFFERS：表明该Surface不包含原生数据，Surface用到的数据由其他对象提供，
        // 在Camera图像预览中就使用该类型的Surface，有Camera负责提供给预览Surface数据，这样图像预览会比较流畅。
        // 如果设置这种类型则就不能调用lockCanvas来获取Canvas对象了
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        capture = (Button) findViewById(R.id.capture);
        editPic = (ImageView) findViewById(R.id.editPic);
        //监听
        capture.setOnClickListener(takePicture);
        //缩略图点击查看监听
        editPic.setOnClickListener(editOnClickListener);


    }

    View.OnClickListener takePicture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isClick) {
                //自动对焦 并回调
                myCamera.autoFocus(MainActivity.this);
                isClick = true;
            } else {
                //可开启预览
                myCamera.startPreview();
                isClick = false;
            }
        }
    };

    View.OnClickListener editOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String picPath = (String) v.getTag();
            Intent intent = new Intent();
            //将图片路径绑定到intent中
            intent.putExtra("path" , picPath);
            intent.setClass(mContext , Picture.class);
            //启动查看图片界面
            startActivity(intent);
            //关闭当前界面
            MainActivity.this.finish();
        }
    };


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (myCamera == null) {
            //得到相机实例
            myCamera = android.hardware.Camera.open();
            try {
                //相机预览传入surfaceholder
                myCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //设置相机参数
        android.hardware.Camera.Parameters params = myCamera.getParameters();
        params.setPictureFormat(PixelFormat.JPEG);
        myCamera.setParameters(params);
        //设置预览方向顺时针旋转90 ，摄像头预览方向与实际相差90度
        myCamera.setDisplayOrientation(90);
        //开始预览
        myCamera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //关闭预览并释放资源
        myCamera.stopPreview();
        myCamera.release();
        myCamera = null;
    }


    @Override
    public void onAutoFocus(boolean success, android.hardware.Camera camera) {
        if (success) {
            //获得参数
            android.hardware.Camera.Parameters params = myCamera.getParameters();
            //设置参数
            params.setPictureFormat(PixelFormat.JPEG);
            myCamera.setParameters(params);
            //拍照 产生三个回调对应 (原始图像 压缩图 jpeg图) 这里只回调了jpeg图
            myCamera.takePicture(null, null, jpeg);
        }
    }

    android.hardware.Camera.PictureCallback jpeg = new android.hardware.Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
            try {
                //获得图片
                Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                SimpleDateFormat sDateFormate = new SimpleDateFormat("yyyyMMddhhmmss");
                String date = sDateFormate.format(new Date());
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(mContext, "SD卡不可用", Toast.LENGTH_SHORT).show();
                } else {
                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    filePath = filePath + "/" + date + ".jpg";
                }

                File file = new File(filePath);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                //创建操作图片用的matrix
                Matrix matrix = new Matrix();
                //图片方向与实际相差90，所以在保存到bos之前将图片顺时针旋转90度
                matrix.postRotate(90);
                //创建新图片
                Bitmap rotateBitmap = Bitmap.createBitmap(bm , 0 , 0 , bm.getWidth() , bm.getHeight() , matrix , true);
                //将图片以jpeg格式压缩到流中
                rotateBitmap.compress(Bitmap.CompressFormat.JPEG , 100 , bos);
                //输出
                bos.flush();
                //关闭
                bos.close();
                //显示缩略图
                editPic.setBackgroundDrawable(changeBitmapToDrawable(rotateBitmap));
                //缩略图点击时得到路径参数
                editPic.setTag(filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private Drawable changeBitmapToDrawable(Bitmap bitmapOrg) {
        int width = bitmapOrg.getWidth();
        int height = bitmapOrg.getHeight();
        //定义要转换成的图片的宽和高
        int newWidth = 100;

        //计算缩放率，新尺寸除以旧尺寸
        float scaleWidth = (float) newWidth/width;
        float scaleHeight = scaleWidth;
        //创建操作图片用的matrix
        Matrix matrix = new Matrix();
        //缩放图片动作
        matrix.postScale(scaleWidth , scaleHeight);
        //创建新图片
        Bitmap resizeBitmap = Bitmap.createBitmap(bitmapOrg , 0 , 0 , width , height , matrix , true);
        //将Bitmap转换为Drawable
        BitmapDrawable bitmapDrawable = new BitmapDrawable(resizeBitmap);
        return bitmapDrawable;
    }


}
