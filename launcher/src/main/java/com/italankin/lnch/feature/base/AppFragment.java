package com.italankin.lnch.feature.base;

import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.fragment.app.Fragment;
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultSender;
import com.italankin.lnch.util.rxjava.WeakDisposableList;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public abstract class AppFragment extends Fragment implements FragmentResultSender {

    private final WeakDisposableList eventsSubs = new WeakDisposableList();

    @Override
    public void sendResult(Bundle result) {
        String requestKey = requireArguments().getString(ARG_REQUEST_KEY);
        getParentFragmentManager().setFragmentResult(requestKey, result);
    }

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
