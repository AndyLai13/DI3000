package com.lightel.di3000;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.google.gson.JsonObject;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final boolean USE_TEXTURE_VIEW = false;
    private static final boolean ENABLE_SUBTITLES = true;
    private static final String ASSET_FILENAME = "bbb.m4v";
    String rtspUrl0 = "rtsp://192.168.1.1/";

    private VLCVideoLayout mVideoLayout = null;

    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    SeekBar seekBarBrightness;
    SeekBar seekBarContrast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");
        mLibVLC = new LibVLC(this, args);
        mMediaPlayer = new MediaPlayer(mLibVLC);
        mVideoLayout = findViewById(R.id.video_layout);

        final Button getModel = findViewById(R.id.get_model);
        getModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFrame();
            }
        });

        seekBarBrightness = findViewById(R.id.seekbar_brightness);
        seekBarContrast = findViewById(R.id.seekbar_contrast);

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

        getDefault();
    }

    void setRes() {
        RestApiManager.getInstance().setRes(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject object = response.body();
                    String Width = object.get("Width").getAsString();
                    Log.d("Andy", "Width = " + Width);
                    String Height = object.get("Height").getAsString();
                    Log.d("Andy", "Height = " + Height);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Andy", "Throwable = " + t);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mLibVLC.release();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMediaPlayer.attachViews(mVideoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW);

        try {
//            final Media media = new Media(mLibVLC, getAssets().openFd(ASSET_FILENAME));
            final Media media = new Media(mLibVLC, Uri.parse(rtspUrl0));
            mMediaPlayer.setMedia(media);
            media.release();
        } catch (Exception e) {
            throw new RuntimeException("Invalid asset folder");
        }
        mMediaPlayer.play();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mMediaPlayer.stop();
        mMediaPlayer.detachViews();
    }

    void getModel() {
        RestApiManager.getInstance().getModel(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject object = response.body();
                    String Model = object.get("Model").getAsString();
                    Log.d("Andy", "Model = " + Model);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Andy", "Throwable = " + t);
            }
        });
    }

    void resetDefault() {

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

    void getFrame() {
        RestApiManager.getInstance().getFrame(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject object = response.body();
//                    int Status = object.get("Status").getAsInt();
                    Log.d("Andy", "object = " + object);
//                    seekBarBrightness.setEnabled(true);
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
                    seekBarContrast.setEnabled(true);
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
                    seekBarContrast.setEnabled(true);
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
                    seekBarContrast.setEnabled(true);
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
                    seekBarContrast.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Andy", "Throwable = " + t);
            }
        }, String.valueOf(exposureTime));
    }
}
