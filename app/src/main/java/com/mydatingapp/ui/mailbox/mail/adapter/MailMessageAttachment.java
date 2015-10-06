package com.mydatingapp.ui.mailbox.mail.adapter;

import com.mydatingapp.ui.mailbox.AttachmentHolderInterface;
import com.mydatingapp.ui.mailbox.mail.image.MailImage;

/**
 * Created by kairat on 3/14/15.
 */
public interface MailMessageAttachment {
    MailImage.Message.Attachment getAttachment();
    void setAttachment(MailImage.Message.Attachment attachment);
    void setListener(MailMessageView view);
    void setListener(AttachmentHolderInterface view);
}
