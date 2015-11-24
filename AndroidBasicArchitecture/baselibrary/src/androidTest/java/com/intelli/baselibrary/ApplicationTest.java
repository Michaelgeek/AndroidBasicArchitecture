package com.intelli.baselibrary;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.ApplicationTestCase;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;

import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.GroupedObservable;
import rx.schedulers.Schedulers;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    private static final String TAG = "ApplicationTest";

    OkHttpClient okHttpClient = new OkHttpClient();
    ImageView imageView = new ImageView(getContext());

    public ApplicationTest() {
        super(Application.class);

        final String bitmapUrl = "https://www.android.com/static/img/ready-for-you/hero-1024.png";
        //可观察者
        rx.Observable<Bitmap> observable = rx.Observable.create(new rx.Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Request request = new Request.Builder().url(bitmapUrl).build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    InputStream is = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    is.close();

                    subscriber.onNext(bitmap);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();

                    subscriber.onError(e.getCause());
                }
            }
        });

        //观察者
        Observer<Bitmap> observer = new Observer<Bitmap>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Bitmap o) {
                imageView.setImageBitmap(o);
            }
        };


//        Subscription sp = observable.subscribe(observer);
        //建立订阅关系
        Subscription sp = observable.subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
        sp.unsubscribe();


        rx.Observable.just("welcome", "to", "Android").subscribe(new Subscriber<String>() {
            @Override
            public void onStart() { //在开始前回调，做准备工作
                super.onStart();
            }

            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(String s) {
                Log.i(TAG, s);
            }
        });

        rx.Observable.just("welcome", "to", "Android").subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.i(TAG, s);
            }
        });

        rx.Observable.just("welcome", "to", "Android").subscribe(
                new Action1<String>() { //处理onNext
                    @Override
                    public void call(String s) {
                        Log.i(TAG, s);
                    }
                }, new Action1<Throwable>() { //处理onError
                    @Override
                    public void call(Throwable throwable) {
                    }
                }, new Action0() {  //处理onCompleted
                    @Override
                    public void call() {
                    }
                });

        String[] words = new String[]{"welcome", "to", "Android"};
        rx.Observable.from(words).map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                return s.toUpperCase();
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.i("", s);
            }
        });

        String uri = "https://www.android.com/static/img/ready-for-you/hero-1024.png";
        rx.Observable.just(uri).subscribeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                    }
                }).subscribeOn(AndroidSchedulers.mainThread()) //指定doOnSubscribe中Action0的Scheduler为UI线程
                .observeOn(Schedulers.io()).map(new Func1<String, Bitmap>() {
            @Override
            public Bitmap call(String s) {
                Request request = new Request.Builder().url(s).build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    InputStream is = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    is.close();

                    return bitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Bitmap>() {
            @Override
            public void call(Bitmap bitmap) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        });

        Integer[] items = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        rx.Observable.from(items).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.computation()).groupBy(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return integer % 2 == 0;
            }
        }).subscribe(new Action1<GroupedObservable<Boolean, Integer>>() {
                         @Override
                         public void call(GroupedObservable<Boolean, Integer> booleanIntegerGroupedObservable) {

                         }
                     }
        );
    }

}