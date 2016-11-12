package info.nukoneko.android.photoeffect.controller.twitter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;

import info.nukoneko.android.photoeffect.R;
import info.nukoneko.android.photoeffect.controller.base.BaseActivity;
import info.nukoneko.android.photoeffect.controller.main.MainActivity;
import info.nukoneko.android.photoeffect.sys.util.rx.Optional;
import info.nukoneko.android.photoeffect.sys.util.rx.RxWrap;
import info.nukoneko.android.photoeffect.sys.util.twitter.TwitterUtils;
import twitter4j.Twitter;
import twitter4j.auth.RequestToken;

/**
 * Created by Atsumi3 on 2014/11/23.
 */
public class TwitterOAuthActivity extends BaseActivity {
    @Nullable
    Twitter mTwitter;
    @Nullable
    RequestToken mTwitterRequestToken;

    private String twitterCallbackUri;

    public static void createInstance(Activity activity) {
        Intent intent = new Intent(activity, TwitterOAuthActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        twitterCallbackUri = getString(R.string.twitter_callback_uri);
        mTwitter = TwitterUtils.getTwitterInstance(this);

        Button button = new Button(this);
        button.setOnClickListener(v -> startAuthorize());
        button.setText("認証する");

        setContentView(button);
    }

    private void startAuthorize() {
        assert mTwitter != null;
        RxWrap.create(bindToLifecycle(), () -> {
            mTwitter.setOAuthAccessToken(null);
            mTwitterRequestToken =
                    mTwitter.getOAuthRequestToken(twitterCallbackUri);
            return mTwitterRequestToken.getAuthorizationURL();
        }).subscribe(s -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(s)));
        }, throwable -> {
            Log.e("failed start auth", throwable.getLocalizedMessage());
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (mTwitter == null) return;

        Optional.ofNullable(intent.getData()).subscribe(uri -> {
            if (!uri.toString().startsWith(twitterCallbackUri)) return;

            RxWrap.create(bindToLifecycle(), () -> {
                return mTwitter.getOAuthAccessToken(mTwitterRequestToken, uri.getQueryParameter("oauth_verifier"));
            }).onErrorReturn(throwable -> null).subscribe(accessToken -> {
                TwitterUtils.storeAccessToken(this, accessToken);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }, throwable -> {
                Log.e("failed get token", throwable.getLocalizedMessage());
            });
        });
    }
}
