package com.italankin.lnch.util.imageloader;

public interface Callback {

    void onSuccess();

    void onError(Exception e);

    Callback EMPTY = new Callback() {
        @Override
        public void onSuccess() {
        }

        @Override
        public void onError(Exception e) {
        }
    };
}
