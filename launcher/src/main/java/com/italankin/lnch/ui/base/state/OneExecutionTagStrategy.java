package com.italankin.lnch.ui.base.state;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.ViewCommand;

import java.util.List;

public class OneExecutionTagStrategy extends TagStrategy {
    @Override
    public <View extends MvpView> void afterApply(List<ViewCommand<View>> currentState, ViewCommand<View> incomingCommand) {
        currentState.remove(incomingCommand);
    }
}