package info.nukoneko.android.photoeffect.sys.util.rx;

import android.app.ProgressDialog;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Atsumi3 on 2016/10/20.
 */

/**
 * create observable for android.
 * if calling from "extends BaseActivity, BaseFragment...",
 *  usable ".compose(usable bindToLifecycle())"
 */
public final class RxWrap {
    private RxWrap(){}

    public interface RxCallable<T> {
        T call() throws Exception;
    }

    private static <T> Observable<T> createBase(RxCallable<T> observable) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                try {
                    subscriber.onNext(observable.call());
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> Observable<T> create(RxCallable<T> observable) {
        return createBase(observable);
    }

    public static <T> Observable<T> create(ProgressDialog progressDialog,
                                           RxCallable<T> observable) {
        return createBase(observable)
                .doOnSubscribe(progressDialog::show)
                .doOnCompleted(progressDialog::dismiss);
    }

    public static <T> Observable<T> create(ProgressDialog progressDialog,
                                           Observable.Transformer<T, T> objectTransformer,
                                           RxCallable<T> observable) {
        return createBase(observable)
                .compose(objectTransformer)
                .doOnSubscribe(progressDialog::show)
                .doOnCompleted(progressDialog::dismiss);
    }

    public static <T> Observable<T> create(Observable.Transformer<T, T> objectTransformer, RxCallable<T> observable) {
        return createBase(observable).compose(objectTransformer);
    }
}
