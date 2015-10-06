package com.mydatingapp.ui.mailbox.mail.adapter;

import com.mydatingapp.ui.mailbox.AttachmentHolderInterface;
import com.mydatingapp.ui.mailbox.RenderInterface;
import com.mydatingapp.ui.mailbox.mail.image.MailImage;

/**
 * Created by kairat on 3/14/15.
 */
public interface MailMessageView extends RenderInterface, AttachmentHolderInterface {
    MailImage.Message getMessage();
    void setMessage(MailImage.Message message);
    void expandMessage();
}
