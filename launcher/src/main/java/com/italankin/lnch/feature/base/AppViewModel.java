package com.italankin.lnch.feature.base;

import androidx.annotation.CallSuper;
import androidx.lifecycle.ViewModel;
import com.italankin.lnch.util.rxjava.WeakDisposableList;
import io.reactivex.CompletableObserver;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public abstract class AppViewModel extends ViewModel {

    private final WeakDisposableList subs = new WeakDisposableList();

    @CallSuper
    @Override
    protected void onCleared() {
        super.onCleared();
        subs.clear();
    }

    protected abstract class State<T> extends BaseState implements Observer<T> {
    }

    protected abstract class SingleState<T> extends BaseState implements SingleObserver<T> {
    }

    protected abstract class CompletableState extends BaseState implements CompletableObserver {
    }

    private abstract class BaseState {
        public void onError(Throwable e) {
            Timber.e(e, "onError:");
        }

        public void onComplete() {
        }

        public void onSubscribe(Disposable d) {
            subs.add(d);
        }
    }
}
