package com.italankin.lnch.feature.settings.base;

import androidx.annotation.CallSuper;
import com.italankin.lnch.util.rxjava.WeakDisposableList;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public abstract class AppPreferenceFragment extends BasePreferenceFragment {

    private final WeakDisposableList eventsSubs = new WeakDisposableList();

    @CallSuper
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        eventsSubs.clear();
    }

    protected abstract class EventObserver<T> implements Observer<T> {
        @Override
        public void onSubscribe(Disposable d) {
            eventsSubs.add(d);
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "onError:");
        }

        @Override
        public void onComplete() {
        }
    }
}
