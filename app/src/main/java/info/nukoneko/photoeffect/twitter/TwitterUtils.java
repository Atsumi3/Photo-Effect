package info.nukoneko.photoeffect.twitter;

import android.content.Context;
import android.content.SharedPreferences;

import info.nukoneko.photoeffect.R;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Atsumi on 2014/11/23.
 */
public class TwitterUtils {
    private static final String TOKEN = "token";
    private static final String TOKEN_SECRET = "token_secret";
    private static final String PREF_NAME = "twitter_access_token";

    public static Twitter getTwitterInstance(Context context) {
        String consumerKey = context.getString(R.string.twitter_api_key);
        String consumerSecret = context.getString(R.string.twitter_api_secret);

        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        if (hasAccessToken(context)) {
            twitter.setOAuthAccessToken(loadAccessToken(context));
        }
        return twitter;
    }

    public static TwitterStream getTwitterStreamInstance(Context context) {
        String consumerKey = context.getString(R.string.twitter_api_key);
        String consumerSecret = context.getString(R.string.twitter_api_secret);
        ConfigurationBuilder builder = new ConfigurationBuilder();
        {
            builder.setOAuthConsumerKey(consumerKey);
            builder.setOAuthConsumerSecret(consumerSecret);
            AccessToken accessToken = loadAccessToken(context);
            if (accessToken != null) {
                builder.setOAuthAccessToken(accessToken.getToken());
                builder.setOAuthAccessTokenSecret(accessToken.getTokenSecret());
            }
        }
        twitter4j.conf.Configuration configuration = builder.build();
        return new TwitterStreamFactory(configuration).getInstance();
    }

    public static void storeAccessToken(Context context, AccessToken accessToken) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN, accessToken.getToken());
        editor.putString(TOKEN_SECRET, accessToken.getTokenSecret());
        editor.apply();
    }

    public static AccessToken loadAccessToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(TOKEN, null);
        String tokenSecret = preferences.getString(TOKEN_SECRET, null);
        if (token != null && tokenSecret != null) {
            return new AccessToken(token, tokenSecret);
        } else {
            return null;
        }
    }

    public static boolean hasAccessToken(Context context) {
        return loadAccessToken(context) != null;
    }

    public static void deleteToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(TOKEN);
        editor.remove(TOKEN_SECRET);
        editor.apply();
    }
}
