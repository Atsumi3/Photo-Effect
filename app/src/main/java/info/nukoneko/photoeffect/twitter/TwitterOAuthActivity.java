package info.nukoneko.photoeffect.twitter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import info.nukoneko.photoeffect.MainActivity;
import info.nukoneko.photoeffect.R;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Created by Atsumi on 2014/11/23.
 */
public class TwitterOAuthActivity extends Activity {

    private String mCallbackURL;
    private Twitter mTwitter;
    private RequestToken mRequestToken;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mCallbackURL = getString(R.string.twitter_callback);
        mTwitter = TwitterUtils.getTwitterInstance(this);
        Button button = new Button(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAuthorize();
            }
        });
        button.setText("認証する");
        setContentView(button);
    }

    private void startAuthorize(){
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try{
                    mRequestToken = mTwitter.getOAuthRequestToken(mCallbackURL);
                    return mRequestToken.getAuthorizationURL();
                }catch (TwitterException e){
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(String url){
                if (url != null){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }else{
                    Toast.makeText(TwitterOAuthActivity.this, "失敗やで..", Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.execute();
    }
    @Override
    public void onNewIntent(Intent intent){
        if ( intent == null || intent.getData() == null || !intent.getData().toString().startsWith(mCallbackURL)){
            return;
        }
        String verifier = intent.getData().getQueryParameter("oauth_verifier");
        AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(String... params) {
                try{
                    return mTwitter.getOAuthAccessToken(mRequestToken, params[0]);
                }catch (TwitterException e){
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(AccessToken accessToken){
                if (accessToken != null){
                    showToast("認証成功！");
                    successOAuth(accessToken);
                }else{
                    showToast("認証失敗。。。");
                }
            }
        };
        task.execute(verifier);
    }
    private void successOAuth(AccessToken accessToken){
        TwitterUtils.storeAccessToken(this, accessToken);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
