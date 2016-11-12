package info.nukoneko.android.photoeffect.controller.main;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import info.nukoneko.android.photoeffect.R;
import info.nukoneko.android.photoeffect.controller.base.BaseActivity;
import info.nukoneko.android.photoeffect.databinding.ActivityMainBinding;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends BaseActivity {
    // ギャラリー参照用
    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;

    private ActivityMainBinding binding;
    private MainActivityViewModel viewModel;

    private Uri bitmapUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        this.viewModel = new MainActivityViewModel(this);
        this.binding.setViewModel(viewModel);
    }

    public ActivityMainBinding getBinding() {
        return binding;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        viewModel.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        viewModel.onTouchEvent(event);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            switch (requestCode) {
                case REQUEST_CAMERA:
                    if (bitmapUri == null) {
                        Toast.makeText(this, R.string.failed_load, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    bitmap = BitmapFactory.decodeFile(bitmapUri.getPath(), new BitmapFactory.Options());
                    String[] paths = {bitmapUri.getPath()};
                    String[] mimeTypes = {"image/*"};
                    MediaScannerConnection
                            .scanFile(this, paths, mimeTypes, (path, uri) -> {});
                    break;

                case REQUEST_GALLERY:
                    bitmapUri = data.getData();
                    try {
                        InputStream is = getContentResolver().openInputStream(data.getData());
                        bitmap = BitmapFactory.decodeStream(is, null, new BitmapFactory.Options());
                        assert is != null;
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;
            }

            viewModel.setImageUri(bitmapUri, bitmap);
            bitmapUri = null;
        }
    }

    public void openCamera() {
        MainActivityPermissionsDispatcher.wakeupCameraWithCheck(this);
    }

    public void openGallery() {
        MainActivityPermissionsDispatcher.wakeupGalleryWithCheck(this);
    }

    /**
     * 撮影のためにカメラを起動します
     */
    @NeedsPermission({
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void wakeupCamera() {
        File dir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                getString(R.string.app_save_path));
        if (!dir.exists() && !dir.mkdirs()) {
            dir = null;
        } else {
            dir = new File(dir, "" + new SimpleDateFormat("yyyMMddHHmmss", Locale.getDefault()).format(new Date()) + ".jpg");
        }

        if (dir == null) {
            this.bitmapUri = null;
        }

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri bitmapUri;
        if (Build.VERSION_CODES.N > Build.VERSION.SDK_INT) {
            bitmapUri = Uri.fromFile(dir);
        } else {
            bitmapUri = FileProvider.getUriForFile(this, getString(R.string.app_save_path), dir);
        }

        i.putExtra(MediaStore.EXTRA_OUTPUT, bitmapUri);
        this.startActivityForResult(i, REQUEST_CAMERA);
        this.bitmapUri = bitmapUri;
    }

    /**
     * 画像取得のためにギャラリーを起動します
     */
    @NeedsPermission({
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void wakeupGallery() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        this.startActivityForResult(Intent.createChooser(intent, "Pick"), REQUEST_GALLERY);
    }
}
