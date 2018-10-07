package com.italankin.lnch.util;

import android.widget.SeekBar;

public final class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
    private final Listener listener;

    public SeekBarChangeListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        listener.onProgressChanged(progress, fromUser);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public interface Listener {
        void onProgressChanged(int progress, boolean fromUser);
    }
}
