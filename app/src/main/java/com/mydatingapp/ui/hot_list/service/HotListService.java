package com.mydatingapp.ui.hot_list.service;

import android.app.Application;
import android.content.Intent;

import com.mydatingapp.core.SkServiceCallbackListener;
import com.mydatingapp.core.SkServiceHelper;
import com.mydatingapp.ui.hot_list.classes.HotListRestRequestCommand;

/**
 * Created by kairat on 12/8/14.
 */
public class HotListService extends SkServiceHelper {
    /**
     * Constructor
     *
     * @param app
     */
    public HotListService(Application app) {
        super(app);
    }

    public int getHotList(SkServiceCallbackListener listener) {
        int requestId = createId();
        Intent i = createIntent(application, new HotListRestRequestCommand(), requestId);

        return runRequest(requestId, i, listener);
    }

    public int addToHotList(SkServiceCallbackListener listener) {
        int requestId = createId();
        Intent i = createIntent(application, new HotListRestRequestCommand(HotListRestRequestCommand.COMMAND.ADD), requestId);

        return runRequest(requestId, i, listener);
    }

    public int removeFromHotList(SkServiceCallbackListener listener) {
        int requestId = createId();
        Intent i = createIntent(application, new HotListRestRequestCommand(HotListRestRequestCommand.COMMAND.REMOVE), requestId);

        return runRequest(requestId, i, listener);
    }
}
