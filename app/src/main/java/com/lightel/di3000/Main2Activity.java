package com.lightel.di3000;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.JsonObject;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Main2Activity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    DrawerLayout mDrawerLayout;
    TextureView mTextureView;
    TextView mLostConnection;
    private MediaPlayer mMediaPlayer;
    private LibVLC mLibVLC = null;
    String rtspUrl0 = "rtsp://192.168.1.1/";
    ZoomImageView2 imageView;
    TextView mTextState;
    SeekBar seekBarBrightness;
    SeekBar seekBarContrast;
    Button btnOption;
    MutableLiveData<Boolean> isFrozen = new MutableLiveData<>();
    MutableLiveData<Boolean> isLostConnection = new MutableLiveData<>();
    WeakHandler mHandler = new WeakHandler(this);

    public static final int MSG_CHECK_CAPTURE_BTN_STATE = 0;
    public static final int MSG_CHECK_PLAY_STATE = 1;

    int oldBtnState = 0;

    static final int ACTION_UP = 0;
    static final int ACTION_DOWN = 1;

    private static class WeakHandler extends Handler {

        private WeakReference<Context> reference;

        public WeakHandler(Context context) {
            reference = new WeakReference<>(context);//这里传入activity的上下文
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_CAPTURE_BTN_STATE:
                    removeMessages(MSG_CHECK_CAPTURE_BTN_STATE);
//                Log.d("Andy", "removeMessages");
                    ((Main2Activity) reference.get()).getCpBtnState();
                    break;
                case MSG_CHECK_PLAY_STATE:
                    removeMessages(MSG_CHECK_PLAY_STATE);
                    ((Main2Activity) reference.get()).checkPlayerState();
                    sendEmptyMessageDelayed(MSG_CHECK_PLAY_STATE,1000);
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // vlc init
        final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");
        mLibVLC = new LibVLC(this, args);
        mMediaPlayer = new MediaPlayer(mLibVLC);
        // live data init
        isFrozen.setValue(false);
        isLostConnection.setValue(false);
        // view init
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mTextureView = findViewById(R.id.texture_view);
        mLostConnection = findViewById(R.id.lost_connection);
        imageView = findViewById(R.id.freeze);
        mTextState = findViewById(R.id.state);
        seekBarBrightness = findViewById(R.id.seekbar_brightness);
        seekBarContrast = findViewById(R.id.seekbar_contrast);
        btnOption = findViewById(R.id.option);
        // set landscape due to player is constrained.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setTextViewSize();

        mTextureView.setSurfaceTextureListener(this);

        Button test = findViewById(R.id.test);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        isFrozen.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isFrozen) {
                // change text
                if (isFrozen) {
                    mTextState.setText(R.string.state_frozen_image);
                    mTextState.setTextColor(Color.RED);
                } else {
                    mTextState.setText(R.string.state_real_time_stream);
                    mTextState.setTextColor(getColor(R.color.blue));
                }
                // capture image
                if (isFrozen) {
                    Bitmap bitmap = mTextureView.getBitmap();
                    imageView.setImageBitmap(bitmap);
                }
                // show/hide captured image
                if (isFrozen) {
                    mTextureView.setVisibility(View.INVISIBLE);
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    mTextureView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                }
            }
        });

        isLostConnection.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLostConnection) {
                if (isLostConnection) {
                    mLostConnection.setVisibility(View.VISIBLE);
                    mTextureView.setVisibility(View.INVISIBLE);
                } else {
                    mLostConnection.setVisibility(View.GONE);
                    mTextureView.setVisibility(View.VISIBLE);
                }
            }
        });

        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setBrightness(seekBar.getProgress());
                seekBarBrightness.setEnabled(false);
            }
        });

        seekBarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setContrast(seekBar.getProgress());
                seekBarContrast.setEnabled(false);
            }
        });

        btnOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        getDefault();
        startQueryCapBtnState();
        mHandler.sendEmptyMessage(MSG_CHECK_PLAY_STATE);
    }

    public void checkPlayerState() {
//        Log.d("Andy", "" + mMediaPlayer.getPlayerState());
        if (mMediaPlayer.getPlayerState() == Media.State.Playing) {
            isLostConnection.setValue(false);
        } else {
            isLostConnection.setValue(true);

        }
    }

    void startQueryCapBtnState() {
        mHandler.sendEmptyMessage(MSG_CHECK_CAPTURE_BTN_STATE);
    }

    void setTextViewSize() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            int landH = Util.getWidth(this);    // 1080
            int landW = Util.getHeight(this);   // 1920

            int viewHeight = landH;
            int viewWidth = (int) (viewHeight * (1280f / 960));
            mTextureView.setLayoutParams(new ConstraintLayout.LayoutParams(viewWidth, viewHeight));
            mLostConnection.setLayoutParams(new ConstraintLayout.LayoutParams(viewWidth, viewHeight));
        }
    }

    private void attachViewSurface() {
        final IVLCVout vlcVout = mMediaPlayer.getVLCVout();
        mMediaPlayer.setScale(0);
        vlcVout.detachViews();
        vlcVout.setVideoView(mTextureView);
        vlcVout.setWindowSize(mTextureView.getWidth(), mTextureView.getHeight());
        vlcVout.attachViews();
        mTextureView.setKeepScreenOn(true);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d("Andy", "onSurfaceTextureAvailable");
        attachViewSurface();
        if (mMediaPlayer.hasMedia()) {
            mMediaPlayer.play();
        } else {
            try {
                Media media = new Media(mLibVLC, Uri.parse(rtspUrl0));
                mMediaPlayer.setMedia(media);
                mMediaPlayer.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d("Andy", "onSurfaceTextureSizeChanged");

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d("Andy", "onSurfaceTextureDestroyed");

        mMediaPlayer.release();
        mLibVLC.release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.d("Andy", "onSurfaceTextureUpdated");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Andy", "mMediaPlayer.getPlayerState() = " + mMediaPlayer.getPlayerState());
        if (mMediaPlayer.getPlayerState() != Media.State.Playing) {
            try {
                Media media = new Media(mLibVLC, Uri.parse(rtspUrl0));
                mMediaPlayer.setMedia(media);
                mMediaPlayer.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getCpBtnState() {
        RestApiManager.getInstance().getCapBtnState(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject object = response.body();
                    int CapBtn = object.get("CapBtn").getAsInt();
                    if (CapBtn == oldBtnState) {
                        if (CapBtn == 0) {
                            // keep not click
//                            Log.d("Andy1", "keep not click ");
                        } else {
                            // keep click
//                            Log.d("Andy1", "keep click ");
                        }
                    } else {
                        if (CapBtn == 1) {
                            // action up
                            Log.d("Andy1", "action down");
                            onKeyEvent(ACTION_DOWN);
                        } else {
                            // action down
                            Log.d("Andy1", "action up");
                            onKeyEvent(ACTION_UP);
                        }
                    }
                    oldBtnState = CapBtn;
                    startQueryCapBtnState();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Andy", "Throwable = " + t);
            }
        });
    }

    void onKeyEvent(int event) {
        switch (event) {
            case ACTION_DOWN:
                break;
            case ACTION_UP:
                if (isFrozen.getValue() != null) {
                    isFrozen.setValue(!isFrozen.getValue());
                }
                break;
        }

    }

    void getDefault() {
        RestApiManager.getInstance().getDefault(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject object = response.body();
                    int brightness = object.get("Brightness").getAsInt();
                    int contrast = object.get("Contrast").getAsInt();
                    int sharpness = object.get("Sharpness").getAsInt();
                    int gain = object.get("Gain").getAsInt();
                    int autoExposure = object.get("AutoExposure").getAsInt();
                    int exposureTime = object.get("ExposureTime").getAsInt();

                    Log.d("Andy", "brightness = " + brightness);
                    Log.d("Andy", "contrast = " + contrast);
                    Log.d("Andy", "sharpness = " + sharpness);
                    Log.d("Andy", "gain = " + gain);
                    Log.d("Andy", "autoExposure = " + autoExposure);
                    Log.d("Andy", "exposureTime = " + exposureTime);
//                    Log.d("Andy", "contrast = " + contrast);
                    seekBarBrightness.setProgress(brightness);
                    seekBarContrast.setProgress(contrast);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Andy", "Throwable = " + t);
            }
        });
    }

    // range: 0 ~ 100
    void setBrightness(int brightness) {
        RestApiManager.getInstance().setBrightness(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject object = response.body();
                    int Status = object.get("Status").getAsInt();
                    Log.d("Andy", "Status = " + Status);
                    seekBarBrightness.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Andy", "Throwable = " + t);
            }
        }, String.valueOf(brightness));
    }

    // range: 0 ~ 100
    void setContrast(int contrast) {
        RestApiManager.getInstance().setContrast(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject object = response.body();
                    int Status = object.get("Status").getAsInt();
                    Log.d("Andy", "Status = " + Status);
                    seekBarContrast.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Andy", "Throwable = " + t);
            }
        }, String.valueOf(contrast));
    }

    // range: 0 ~ 100
    void setSharpness(int sharpness) {
        RestApiManager.getInstance().setSharpness(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject object = response.body();
                    int Status = object.get("Status").getAsInt();
                    Log.d("Andy", "Status = " + Status);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Andy", "Throwable = " + t);
            }
        }, String.valueOf(sharpness));
    }

    // range: 0 ~ 100
    void setGain(int gain) {
        RestApiManager.getInstance().setGain(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject object = response.body();
                    int Status = object.get("Status").getAsInt();
                    Log.d("Andy", "Status = " + Status);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Andy", "Throwable = " + t);
            }
        }, String.valueOf(gain));
    }

    // range: 0 or 1
    void setAutoExposure(int autoExposure) {
        RestApiManager.getInstance().setAutoExposure(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject object = response.body();
                    int Status = object.get("Status").getAsInt();
                    Log.d("Andy", "Status = " + Status);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Andy", "Throwable = " + t);
            }
        }, String.valueOf(autoExposure));
    }

    // range: -13~-1
    void setExposureTime(int exposureTime) {
        RestApiManager.getInstance().setExposureTime(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject object = response.body();
                    int Status = object.get("Status").getAsInt();
                    Log.d("Andy", "Status = " + Status);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Andy", "Throwable = " + t);
            }
        }, String.valueOf(exposureTime));
    }
}
