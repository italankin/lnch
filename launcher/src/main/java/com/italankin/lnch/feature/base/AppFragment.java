package com.italankin.lnch.feature.base;

import android.os.Bundle;
import com.arellomobile.mvp.MvpAppCompatFragment;
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultSender;
import com.italankin.lnch.util.rxjava.WeakDisposableList;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public abstract class AppFragment extends MvpAppCompatFragment implements FragmentResultSender {

    protected final WeakDisposableList subs = new WeakDisposableList();

    @Override
    public void sendResult(Bundle result) {
        String requestKey = requireArguments().getString(ARG_REQUEST_KEY);
        getParentFragmentManager().setFragmentResult(requestKey, result);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subs.clear();
    }

    protected abstract class EventObserver<T> implements Observer<T> {
        @Override
        public void onSubscribe(Disposable d) {
            subs.add(d);
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
