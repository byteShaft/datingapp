package com.mydatingapp.ui.mailbox.mail;

import com.mydatingapp.ui.mailbox.ConversationItemHolder;
import com.mydatingapp.ui.mailbox.conversation_list.model.ConversationList;
import com.mydatingapp.ui.mailbox.mail.adapter.MailMessageAttachment;
import com.mydatingapp.ui.mailbox.mail.adapter.MailMessageView;
import com.mydatingapp.ui.mailbox.mail.image.MailImage;

/**
 * Created by kairat on 3/14/15.
 */
public interface MailView extends ConversationItemHolder {
    void loadConversationData(MailImage image);
    void setUnreadMessageCount(int count);
    void onUnreadConversation();
    void onDeleteConversation();
    void clearConversationList();
    void onMailMessageClick(MailMessageView view);
    void onMailAttachmentClick(MailMessageAttachment attachment);
    void reply(ConversationList.ConversationItem conversationItem);
    void onAuthorizeResponse(MailImage.Message message);
    void onWinkBackResponse(MailImage.Message message);
}
