package info.nukoneko.android.photoeffect.sys.util.file;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import info.nukoneko.android.photoeffect.sys.util.rx.Optional;

/**
 * Created by Atsumi3 on 2016/10/20.
 */

public final class NKFileUtil {
    private NKFileUtil() {
    }

    /**
     * インテント後のURIから実際のパスを取得する
     *
     * @param context context
     * @param data    data
     * @return uri
     */
    @Nullable
    public static Uri getPathFromIntent(@NonNull Context context, Intent data) {
        Uri returnUri = null;
        if (Build.VERSION.SDK_INT >= 19) {
            returnUri = NKFilePathUtil.getPath(context, data.getData());
            if (returnUri == null) {
                Optional.ofNullable(data.getData()).subscribe(uri -> {
                    String errorText =
                            String.format("URI\t\t: %s\nID\t\t: %s\nAUTHORITY\t: %s",
                                    uri.toString(),
                                    DocumentsContract.getDocumentId(uri),
                                    uri.getAuthority());
                    Log.i("failed load file path", errorText);
                });
            }
        } else {
            Cursor cursor = context.getContentResolver().query(
                    data.getData(),
                    new String[]{MediaStore.Images.Media.DATA
                    }, null, null, null);

            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                returnUri = Uri.parse(cursor.getString(column_index));
                cursor.close();
            }
        }
        return returnUri;
    }
}
