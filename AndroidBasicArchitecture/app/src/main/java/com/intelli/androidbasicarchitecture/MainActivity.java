package com.intelli.androidbasicarchitecture;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.GroupedObservable;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Integer[] items = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        Observable.from(items).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.computation()) //指定过滤操作在Schedulers.computation()上执行
                .filter(new Func1<Integer, Boolean>() { //执行过滤操作
                    @Override
                    public Boolean call(Integer integer) {
                        return integer % 2 == 0;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()) //指定ui线程执行Observer
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Integer integer) { //处理过滤的数据
                        Log.i("", "" + integer);
                    }
                });

        rx.Observable.from(items)
                .groupBy(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) { //此处返回integer所属组的key
                        return integer % 2 == 0;
                    }
                })
                .subscribe(new Action1<GroupedObservable<Boolean, Integer>>() {
                               @Override
                               public void call(GroupedObservable<Boolean, Integer> booleanIntegerGroupedObservable) {
                                   boolean key = booleanIntegerGroupedObservable.getKey();
                                   if (key) {
                                       //当前的GroupedObservable偶数分组
                                       booleanIntegerGroupedObservable.cache().subscribe(new Action1<Integer>() {
                                           @Override
                                           public void call(Integer integer) {
                                               Log.d("", integer + "");
                                           }
                                       });
                                   } else {
                                       //当前GroupedObservable为奇数分组，丢弃
                                       booleanIntegerGroupedObservable.take(0);
                                   }
                               }
                           }
                );
    }
}
