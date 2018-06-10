package com.example.liuyan.mycamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ZoomControls;

public class Picture extends AppCompatActivity {

    ImageView iv;
    ZoomControls zoom;
    //屏幕显示区域宽度 高度
    private int displayWidth;
    private int displayHeight;
    private float scaleWidth = 1;
    private float scaleHeight = 1;
    //图片宽度 高度
    int bmpWidth;
    int bmpHeight;
    Bitmap bitmapOrg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture);

        zoom = (ZoomControls) findViewById(R.id.zoomControls1);
        //获取屏幕分辨率大小
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        displayWidth = dm.widthPixels;
        //减去ZoomControls的高度
        displayHeight = dm.heightPixels - 80;
        zoom.setIsZoomInEnabled(true); //放大
        zoom.setIsZoomOutEnabled(true); //缩小

        iv = (ImageView) findViewById(R.id.img);
        Intent it = getIntent();
        String picPath = (String) it.getCharSequenceExtra("path");
        //从图片存储加载Bitmap
        bitmapOrg = BitmapFactory.decodeFile(picPath , null);
        iv.setImageBitmap(bitmapOrg);
        //获取图片的宽度 高度
        bmpWidth = bitmapOrg.getWidth();
        bmpHeight = bitmapOrg.getHeight();

        //图片放大
        zoom.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置图片放大倍数
                double scale = 1.25;
                //计算这次要放大的倍数
                scaleWidth = (float) (scaleWidth*scale);
                scaleHeight = (float) (scaleHeight*scale);
                //图片不放太大，防止超过内存
                if (scaleWidth > 1.25){
                    scaleWidth = 1;
                    scaleHeight = 1;
                }
                //产生新的Bitmap对象
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth , scaleHeight);
                Bitmap resizeBmp = Bitmap.createBitmap(bitmapOrg , 0 , 0 , bmpWidth ,
                        bmpHeight , matrix , true
                        );
                iv.setImageBitmap(resizeBmp);
            }
        });

        //图片缩小
        zoom.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置图片放大比例
                double scale = 0.8;
                //计算此次要放大的比例
                scaleWidth = (float) (scaleWidth*scale);
                scaleHeight = (float) (scaleHeight*scale);
                //产生新的Bitmap
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth , scaleHeight);
                Bitmap resizeBmp = Bitmap.createBitmap(bitmapOrg , 0 , 0 , bmpWidth ,
                        bmpHeight , matrix , true
                        );
                iv.setImageBitmap(resizeBmp);
            }
        });
    }


    //添加菜单项 返回和退出
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,1,0,"返回");
        menu.add(0,2,0,"退出");
        return true;
    }

    //处理菜单点击
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case 1:
                Intent intent = new Intent();
                intent.setClass(this , MainActivity.class);
                startActivity(intent);
                this.finish();
                return true;
            case 2:
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
