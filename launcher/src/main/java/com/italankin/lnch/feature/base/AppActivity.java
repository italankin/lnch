package com.italankin.lnch.feature.base;

import androidx.appcompat.app.AppCompatActivity;
import com.italankin.lnch.util.rxjava.WeakDisposableList;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public abstract class AppActivity extends AppCompatActivity {

    private final WeakDisposableList eventsSubs = new WeakDisposableList();

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
