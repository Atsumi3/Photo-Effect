package info.nukoneko.android.photoeffect.sys.util.rx;

import android.support.annotation.Nullable;

import rx.Observable;
import rx.functions.Func0;

/**
 * Created by Atsumi3 on 2016/10/20.
 */

public final class Optional{
    private Optional(){}

    public static <T> Observable<T> of(T data){
        if(data == null){
            return Observable.create(subscriber -> {
                subscriber.onError(new NullPointerException());
            });
        }
        else{
            return Observable.just(data);
        }
    }

    public static <T> Observable<T> ofNullable(T data){
        if(data == null){
            return Observable.empty();
        }
        else{
            return Observable.just(data);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Observable<T> ofParsable(@Nullable Object object, Class<T> clazz){
        if(clazz.isInstance(object)){
            return Observable.just((T) object);
        }
        else{
            return Observable.empty();
        }
    }

    public static <T> T get(Observable<T> observable){
        return observable.toBlocking().single();
    }

    public static <T> T orElse(Observable<T> observable, T defaultValue){
        return observable.defaultIfEmpty(defaultValue).toBlocking().single();
    }

    public static <T> boolean isPresent(Observable<T> observable){
        return observable.isEmpty().toBlocking().single();
    }

    public static <T> T orElseGet(Observable<T> observable, Func0<T> other) {
        return isPresent(observable) ? get(observable) : other.call();
    }

    public static <T, X extends Throwable> T orElseThrow(Observable<T> observable, Func0<? extends X> other)
            throws X {
        if (isPresent(observable)) {
            return get(observable);
        } else {
            throw other.call();
        }
    }
}
