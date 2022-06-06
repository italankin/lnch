package com.italankin.lnch.feature.base;

import com.arellomobile.mvp.MvpPresenter;
import com.arellomobile.mvp.MvpView;

import io.reactivex.CompletableObserver;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public abstract class AppPresenter<V extends MvpView> extends MvpPresenter<V> {
    private final CompositeDisposable subs = new CompositeDisposable();

    @Override
    public void onDestroy() {
        super.onDestroy();
        subs.clear();
    }

    protected abstract class State<T> extends BaseState implements Observer<T> {
        protected void onNext(V viewState, T t) {
        }

        @Override
        public void onNext(T t) {
            onNext(getViewState(), t);
        }
    }


    protected abstract class SingleState<T> extends BaseState implements SingleObserver<T> {
        protected void onSuccess(V viewState, T t) {
        }

        @Override
        public void onSuccess(T t) {
            onSuccess(getViewState(), t);
        }
    }

    protected abstract class CompletableState extends BaseState implements CompletableObserver {
        protected void onComplete(V viewState) {
        }

        @Override
        public void onComplete() {
            onComplete(getViewState());
        }
    }

    private abstract class BaseState {
        protected void onError(V viewState, Throwable e) {
        }

        public void onError(Throwable e) {
            V viewState = getViewState();
            Timber.e(e, "onError(viewState=%s)", viewState.getClass().getSimpleName());
            onError(viewState, e);
        }

        public void onComplete() {
        }

        public void onSubscribe(Disposable d) {
            subs.add(d);
        }
    }
}
