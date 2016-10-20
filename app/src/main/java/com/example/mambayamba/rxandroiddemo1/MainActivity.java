package com.example.mambayamba.rxandroiddemo1;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.internal.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.recycler)RecyclerView recycler;
    @BindView(R.id.button) Button button;
    @BindView(R.id.refresher)SwipeRefreshLayout refresher;
    private RxAdapter rxAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        refresher.setOnRefreshListener(this);
    }

    private Observable<List<ResolveInfo>> createResolveObservable(){
        //создаём обозреваемый объект, достаём с помощью интент фильтра информацию о приложениях
        return Observable.create(subscriber -> {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolverList = getPackageManager().queryIntentActivities(intent, 0);

            for(ResolveInfo info: resolverList)
                Log.d("happy", info.toString()+"\n");

            subscriber.onNext(resolverList);    //передаём список приложений обозревателю
            if(!subscriber.isUnsubscribed())    //если подспписка есть -> завершаем передачу
                subscriber.onCompleted();
        });
    }
    private void subscribeOnResolveObservable(Observable<List<ResolveInfo>> observable){
        observable.subscribe(new Subscriber<List<ResolveInfo>>() {
            @Override
            public void onCompleted() {
                Log.d("happy", "onCompleted");
                Toast.makeText(MainActivity.this, "Here is the list!", Toast.LENGTH_SHORT).show();
                refresher.setRefreshing(false);
            }
            @Override
            public void onError(Throwable e) {
                Log.d("happy", "onError: "+e);
                refresher.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNext(List<ResolveInfo> appInfos) {
                Log.d("happy", "onNext");
                rxAdapter = new RxAdapter(appInfos);
                recycler.setAdapter(rxAdapter);
            }
        });
    }

    @Override
    public void onRefresh() {
        subscribeOnResolveObservable(createResolveObservable());
    }

    @OnClick(R.id.button)
    public void onClick(){
        subscribeOnResolveObservable(createResolveObservable());
        button.setVisibility(View.INVISIBLE);
        button.setActivated(false);
    }

    static class RxHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.holder_image)ImageView holderImage;
        @BindView(R.id.holder_text)TextView holderText;

        public RxHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private class RxAdapter extends RecyclerView.Adapter<RxHolder>{
        private List<ResolveInfo> list;

        public RxAdapter(List<ResolveInfo> list) {
            this.list = list;
        }

        @Override
        public RxHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            return new RxHolder(inflater.inflate(R.layout.holder_rx, parent, false));
        }

        @Override
        public void onBindViewHolder(RxHolder holder, int position) {
            holder.holderText.setText(list.get(position).loadLabel(getPackageManager()));
            holder.holderImage.setImageDrawable(list.get(position).loadIcon(getPackageManager()));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

}
