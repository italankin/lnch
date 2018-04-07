package com.italankin.lnch.ui.base;

import android.util.Log;

import com.arellomobile.mvp.MvpPresenter;
import com.arellomobile.mvp.MvpView;

import rx.Observer;
import rx.subscriptions.CompositeSubscription;

public abstract class AppPresenter<V extends MvpView> extends MvpPresenter<V> {
    protected final CompositeSubscription subs = new CompositeSubscription();

    @Override
    public void onDestroy() {
        super.onDestroy();
        subs.clear();
    }

    protected abstract class State<T> implements Observer<T> {
        protected void onNext(V viewState, T t) {
        }

        protected void onError(V viewState, Throwable e) {
        }

        @Override
        public void onNext(T v) {
            onNext(getViewState(), v);
        }

        @Override
        public void onError(Throwable e) {
            Log.e(AppPresenter.this.getClass().getSimpleName(), "State.onError:", e);
            onError(getViewState(), e);
        }

        @Override
        public void onCompleted() {
        }
    }
}
