package com.italankin.lnch.feature.home.apps.events;

import com.italankin.lnch.feature.home.model.Update;

public interface UpdateEvent {

    class Success implements UpdateEvent {
        public final Update update;

        public Success(Update update) {
            this.update = update;
        }
    }

    class Error implements UpdateEvent {
        public final Throwable error;

        public Error(Throwable error) {
            this.error = error;
        }
    }
}
