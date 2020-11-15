package com.italankin.lnch.feature.widgets;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.repository.prefs.WidgetsState;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@InjectViewState
public class WidgetsPresenter extends AppPresenter<WidgetsView> {

    private final WidgetsState widgetsState;

    @Inject
    WidgetsPresenter(WidgetsState widgetsState) {
        this.widgetsState = widgetsState;
    }

    void loadWidgets() {
        widgetsState.loadWidgetIds()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<List<Integer>>() {
                    @Override
                    protected void onSuccess(WidgetsView viewState, List<Integer> appWidgetIds) {
                        viewState.onBindWidgets(appWidgetIds);
                    }
                });
    }

    void addWidget(int appWidgetId) {
        widgetsState.addWidgetId(appWidgetId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                    }
                });
    }

    void removeWidget(int appWidgetId) {
        widgetsState.removeWidgetId(appWidgetId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                    }
                });
    }
}
