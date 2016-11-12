package info.nukoneko.android.photoeffect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import info.nukoneko.android.photoeffect.twitter.TwitterOAuthActivity;
import info.nukoneko.android.photoeffect.twitter.TwitterUtils;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBulgeDistortionFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageEmbossFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageExposureFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGammaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGaussianBlurFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHighlightShadowFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageOpacityFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSaturationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSharpenFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSobelEdgeDetection;
import jp.co.cyberagent.android.gpuimage.GPUImageSobelThresholdFilter;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;


public class MainActivity extends Activity {

    // 実装しているフィルタの種類
    enum FilterType {
        Normal,
        Sepia,
        Gray,
        Sharp,
        Edge,
        Gamma,
        Brightness,
        BulgeDistortion,
        Shapen,
        Emboss,
        GaussianBlur,
        HighLightShadow,
        Saturation,
        Exposure,
        Contrast,
        Opacity,
        Threshold
    }

    // いま何のフィルタを選択しているか　　デフォルトはｾﾋﾟｱ
    FilterType nowFilter = FilterType.Sepia;

    Animation inAnim;
    Animation outAnim;

    // 入れ物だけ宣言
    TextView filterType;

    // ギャラリー参照用
    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;
    private Uri bitmapUri;

    GPUImage gpuImage;
    ImageView imageView;
    Twitter twitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // 入れ物に中身入れる
        if(gpuImage == null) gpuImage = new GPUImage(this);
        imageView = (ImageView)findViewById(R.id.gpuimage);

        filterType = (TextView) findViewById(R.id.filter_type);
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seek);
        final TextView textView = (TextView) findViewById(R.id.value);

        // シークバーが操作されたときの処理
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // シークバーの最大値は 100
                // 取得したい値は 0 ~ 1 の値なので /100 する
                // 関数に後の処理はお任せ
                float val = (float) progress / 100;
                textView.setText("VALUE: " + String.valueOf(val));
                changeFilter(val);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // アニメーションをセット
        inAnim = AnimationUtils.loadAnimation(this, R.anim.in_anim);
        outAnim = AnimationUtils.loadAnimation(this, R.anim.out_anim);

        // 画像を押したときに消すレイアウトグループを呼ぶ
        final RelativeLayout relativeLayout1 = (RelativeLayout) findViewById(R.id.menu_top);
        final RelativeLayout relativeLayout2 = (RelativeLayout) findViewById(R.id.menu_bottom);
        // 画像を押したときの処理
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationView(relativeLayout1);
                animationView(relativeLayout2);
            }
        });

        twitter = TwitterUtils.getTwitterInstance(this);

        try{
            gpuImage.getBitmapWithFilterApplied();
        }catch (NullPointerException e) {
            getPicture();
        }

        setButtonOnClickEvent();

    }

    // それぞれボタンを押したときの処理を設定する関数
    public void setButtonOnClickEvent() {

        // 各ボタンをクリックしたときの処理
        // id でそれぞれのボタンを選び、setOnClickListener で処理
        findViewById(R.id.b_sepia).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // セピアのボタンが押されたので、nowFilter にセピアを選択
                // とりあえず画像にフィルタつけるためにchangeFilterを呼ぶ
                nowFilter = FilterType.Sepia;
                changeFilter((float) 1);
            }
        });
        //以下同文
        findViewById(R.id.b_gray).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.Gray;
                changeFilter((float) 1);
            }
        });
        findViewById(R.id.b_sharp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.Sharp;
                changeFilter((float) 1);
            }
        });
        findViewById(R.id.b_edge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.Edge;
                changeFilter((float) 1);
            }
        });
        findViewById(R.id.b_bamma).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.Gamma;
                changeFilter((float) 1);
            }
        });
        findViewById(R.id.b_brightness).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.Brightness;
                changeFilter((float) 1);
            }
        });
        findViewById(R.id.b_bulge_distortion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.BulgeDistortion;
                changeFilter((float) 1);
            }
        });
        findViewById(R.id.b_shapen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.Shapen;
                changeFilter((float) 1);
            }
        });
        findViewById(R.id.b_emboss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.Emboss;
                changeFilter((float) 1);
            }
        });
        findViewById(R.id.b_gaussian_blur).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.GaussianBlur;
                changeFilter((float) 1);
            }
        });
        findViewById(R.id.b_high_light).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.HighLightShadow;
                changeFilter((float) 1);
            }
        });
        findViewById(R.id.b_saturation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.Saturation;
                changeFilter((float) 1);
            }
        });
        findViewById(R.id.b_exposure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.Exposure;
                changeFilter((float) 1);
            }
        });
        findViewById(R.id.b_contrast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.Contrast;
                changeFilter((float) 1);
            }
        });
        findViewById(R.id.b_opacity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.Opacity;
                changeFilter((float) 1);
            }
        });
        findViewById(R.id.b_threshold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowFilter = FilterType.Threshold;
                changeFilter((float) 1);
            }
        });

        // 上部メニュー

        // ロードボタンを押したときの処理
        findViewById(R.id.load_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPicture();
            }
        });

        // ツイートボタンを押したときの処理
        findViewById(R.id.tweet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitterMenu();
            }
        });
    }

    // 画像を押したときにアニメーションをさせる関数
    public void animationView(View view) {
        if (view.getVisibility() == View.GONE) {
            view.setAnimation(inAnim);
            view.setVisibility(View.VISIBLE);
        } else {
            view.startAnimation(outAnim);
            view.setVisibility(View.GONE);
        }
    }

    // 画像にフィルタをかける関数
    public void changeFilter(float val) {
        // 今何のフィルタが選択されているか分岐
        switch (nowFilter) {
            // Sepia のケース (場合)
            case Sepia:
                // gpuImage にフィルタと値をセット
                gpuImage.setFilter(new GPUImageSepiaFilter(val));
                break;
            // Gray のケース (場合)
            case Gray:
                gpuImage.setFilter(new GPUImageGrayscaleFilter());
                break;
            case Sharp:
                gpuImage.setFilter(new GPUImageSharpenFilter(val));
                break;
            case Edge:
                gpuImage.setFilter(new GPUImageSobelEdgeDetection());
                break;
            case Gamma:
                gpuImage.setFilter(new GPUImageGammaFilter(val));
                break;
            case Brightness:
                gpuImage.setFilter(new GPUImageBrightnessFilter(val));
                break;
            case BulgeDistortion:
                gpuImage.setFilter(new GPUImageBulgeDistortionFilter());
                break;
            case Shapen:
                gpuImage.setFilter(new GPUImageSharpenFilter(val));
                break;
            case Emboss:
                gpuImage.setFilter(new GPUImageEmbossFilter(val));
                break;
            case GaussianBlur:
                gpuImage.setFilter(new GPUImageGaussianBlurFilter(val));
                break;
            case HighLightShadow:
                gpuImage.setFilter(new GPUImageHighlightShadowFilter(val, (float) 0.5));
                break;
            case Saturation:
                gpuImage.setFilter(new GPUImageSaturationFilter(val));
                break;
            case Exposure:
                gpuImage.setFilter(new GPUImageExposureFilter(val));
                break;
            case Contrast:
                gpuImage.setFilter(new GPUImageContrastFilter(val));
                break;
            case Opacity:
                gpuImage.setFilter(new GPUImageOpacityFilter(val));
                break;
            case Threshold:
                gpuImage.setFilter(new GPUImageSobelThresholdFilter(val));
                break;
            default:
                break;
        }
        try {
            // gpuImageView に フィルタがかかった画像をセット
            imageView.setImageBitmap(gpuImage.getBitmapWithFilterApplied());
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        filterType.setText("TYPE: " + nowFilter.toString());

    }

    // Twitterのボタンを押したときに出るメニュー
    protected void twitterMenu() {

        final String[] str_items = {
                getString(R.string.tweet_ok),
                getString(R.string.twitter_account_reauth),
                getString(R.string.cancel)
        };
        new AlertDialog.Builder(MainActivity.this).setTitle(getString(R.string.twitter_menu)).setItems(str_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (str_items.length > which && which > -1) {
                    switch (which) {
                        case 0:
                            try {
                                tweetDialog();
                            } catch (TwitterException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 1:
                            TwitterUtils.deleteToken(MainActivity.this);
                            Intent intent = new Intent(MainActivity.this, TwitterOAuthActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        default:
                            break;
                    }
                }
            }
        }).show();
    }

    // 画像をどこから取得するかの選択肢を出す関数
    protected void getPicture() {

        final String[] str_items = {
                getString(R.string.load_camera),
                getString(R.string.load_gallery),
                getString(R.string.cancel)
        };
        new AlertDialog.Builder(MainActivity.this).setTitle(getString(R.string.load_image)).setItems(str_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (str_items.length > which && which > -1) {
                    switch (which) {
                        case 0:
                            wakeUpCamera();
                            break;
                        case 1:
                            wakeUpGallery();
                            break;
                        default:
                            break;
                    }
                }
            }
        }).show();
    }

    // ギャラリー、カメラから戻ってきたときの処理をする関数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            Bitmap bm = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // 元の1/4サイズでbitmap取得

            switch (requestCode) {
                case REQUEST_CAMERA:
                    System.out.println(bitmapUri.getPath());
                    bm = BitmapFactory.decodeFile(bitmapUri.getPath(), options);
                    // 撮影した画像をギャラリーのインデックスに追加されるようにスキャンする。
                    // これをやらないと、アプリ起動中に撮った写真が反映されない
                    String[] paths = {bitmapUri.getPath()};
                    String[] mimeTypes = {"image/*"};
                    MediaScannerConnection.scanFile(getApplicationContext(), paths, mimeTypes, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
                    break;

                case REQUEST_GALLERY:
                    try {
                        if (data != null || data.getData() != null) {
                            bitmapUri = data.getData();
                            InputStream is = getContentResolver().openInputStream(data.getData());
                            bm = BitmapFactory.decodeStream(is, null, options);
                            is.close();
                        }
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                        bm = null;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    finish();
                    break;
            }

            ExifInterface exif = null;
            try {
                exif = new ExifInterface(bitmapUri.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            }
            Matrix matrix = new Matrix();

            if(exif != null) {
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
            }
            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

            gpuImage.setImage(bm);
            imageView.setImageBitmap(bm);

        } else {
            finish();
        }
    }

    // カメラを立ち上げる関数
    protected void wakeUpCamera() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ), "PictureSaveDir"
        );
        if (!mediaStorageDir.exists() & !mediaStorageDir.mkdir()) {
            return;
        }
        String timeStamp = new SimpleDateFormat("yyyMMddHHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + ".JPG");
        bitmapUri = Uri.fromFile(mediaFile);
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, bitmapUri); // 画像をmediaUriに書き込み
        startActivityForResult(i, REQUEST_CAMERA);
    }

    // ギャラリーを立ち上げる関数
    protected void wakeUpGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    // 画面回転時の問題を解決する関数 いらん。
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // ツイートする関数 画像ありき
    void tweet(String s) {

        showNotification(R.drawable.ic_launcher, getString(R.string.tweet_result), getString(R.string.tweeting), 1);
        AsyncTask<String, Void, Status> task = new AsyncTask<String, Void, Status>() {
            @Override
            protected twitter4j.Status doInBackground(String... params) {
                try {
                    final StatusUpdate statusUpdate = new StatusUpdate(String.valueOf(params[0]));

                    try{
                        gpuImage.getBitmapWithFilterApplied();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        gpuImage.getBitmapWithFilterApplied().compress(Bitmap.CompressFormat.PNG, 100, bos);
                        InputStream inputStream = new ByteArrayInputStream(bos.toByteArray());
                        bos.close();

                        statusUpdate.media("test", inputStream);
                        inputStream.close();
                    }catch (NullPointerException ignored){

                    }
                    return twitter.updateStatus(statusUpdate);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(twitter4j.Status status) {
                if (status != null) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(1);

                    Toast.makeText(MainActivity.this, getString(R.string.tweet_success), Toast.LENGTH_SHORT).show();
                    showNotification(R.drawable.ic_launcher, getString(R.string.tweet_result), getString(R.string.tweet_success), 1);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.tweet_failed), Toast.LENGTH_SHORT).show();
                    showNotification(R.drawable.ic_launcher, getString(R.string.tweet_result), getString(R.string.tweet_failed), 1);
                }
            }
        };
        task.execute(s);
    }

    // 通知バーにお知らせを表示する関数
    public void showNotification(int image, String title, String text, int id) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(image)
                .setContentTitle(title)
                .setContentText(text);

        Intent intent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);
        builder.setTicker(text);
        builder.setDefaults(Notification.DEFAULT_VIBRATE);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, builder.build());
    }

    // ツイート内容を書く関数
    private void tweetDialog() throws TwitterException {
        // 認証済みだったら
        if (TwitterUtils.hasAccessToken(this)) {
            final EditText editText = new EditText(this);
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.tweet_body))
                    .setView(editText)
                    .setPositiveButton(getString(R.string.tweet_send), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tweet(editText.getText().toString());
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .create();

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            });
            alertDialog.show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.no_set_account))
                    .setPositiveButton(getString(R.string.twitter_auth), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(MainActivity.this, TwitterOAuthActivity.class));
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .create().show();
        }
    }
}
