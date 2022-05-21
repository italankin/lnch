package com.italankin.lnch.feature.home.fragmentresult;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.LifecycleOwner;
import timber.log.Timber;

public class FragmentResultManager implements FragmentResultListener {

    private final FragmentManager fragmentManager;
    private final LifecycleOwner lifecycleOwner;
    private final String requestKey;

    private final Map<String, FragmentResultContract<?>> contracts = new HashMap<>(4);
    private final Map<FragmentResultContract<?>, FragmentResultContractListener<?>> contractListeners = new HashMap<>(4);

    private OnUnhandledResultListener unhandledResultListener = new DefaultOnUnhandledResultListener();

    public FragmentResultManager(FragmentManager fragmentManager, LifecycleOwner lifecycleOwner, String requestKey) {
        this.fragmentManager = fragmentManager;
        this.lifecycleOwner = lifecycleOwner;
        this.requestKey = requestKey;
    }

    public void attach() {
        fragmentManager.setFragmentResultListener(requestKey, lifecycleOwner, this);
    }

    public void detach() {
        fragmentManager.clearFragmentResultListener(requestKey);
    }

    public FragmentResultManager setUnhandledResultListener(OnUnhandledResultListener listener) {
        this.unhandledResultListener = listener;
        return this;
    }

    public <T> FragmentResultManager register(FragmentResultContract<T> contract, FragmentResultContractListener<? super T> listener) {
        String contractKey = contract.key();
        if (contracts.containsKey(contractKey)) {
            throw new IllegalStateException("duplicate contract=" + contractKey + ": requestKey=" +
                    requestKey + " already has " + contracts.get(contractKey));
        }
        contracts.put(contractKey, contract);
        contractListeners.put(contract, listener);
        return this;
    }

    public void unregister(FragmentResultContract<?> contract) {
        contractListeners.remove(contract);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
        if (!this.requestKey.equals(requestKey)) {
            return;
        }
        String key = result.getString(FragmentResultContract.RESULT_KEY);
        Timber.d("[%s] onFragmentResult: key=%s result=%s", this.requestKey, key, result);
        FragmentResultContract<?> contract = contracts.get(key);
        if (contract != null) {
            FragmentResultContractListener<Object> listener =
                    (FragmentResultContractListener<Object>) contractListeners.get(contract);
            if (listener != null) {
                Object parseResult = contract.parseResult(result);
                listener.onResult(parseResult);
                return;
            }
        } else {
            Timber.w("[%s] no contract found for key=%s", this.requestKey, key);
        }
        if (unhandledResultListener != null) {
            unhandledResultListener.onUnhandledResult(key, result);
        }
    }

    public interface OnUnhandledResultListener {
        void onUnhandledResult(String key, Bundle result);
    }

    private class DefaultOnUnhandledResultListener implements OnUnhandledResultListener {
        @Override
        public void onUnhandledResult(String key, Bundle result) {
            Timber.tag("FragmentResultManager").w("[%s] unhandled result: key=%s, result=%s", requestKey, key, result);
        }
    }
}
