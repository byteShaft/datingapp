package com.mydatingapp.ui.mailbox.chat.model;

import com.mydatingapp.ui.mailbox.RenderInterface;

/**
 * Created by kairat on 2/25/15.
 */
public interface DailyHistory extends RenderInterface {
    ConversationHistory.DailyHistory getHistory();
    Boolean addHistory(final ConversationHistory.DailyHistory dailyHistory);
    void rebuild();
}
