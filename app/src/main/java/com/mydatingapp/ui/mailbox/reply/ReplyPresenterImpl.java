package com.mydatingapp.ui.mailbox.reply;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mydatingapp.R;
import com.mydatingapp.ui.mailbox.AuthorizationInfo;
import com.mydatingapp.ui.mailbox.chat.model.ConversationHistory;
import com.mydatingapp.ui.mailbox.mail.image.MailImage;
import com.mydatingapp.utils.SkApi;

import java.io.File;
import java.io.IOException;

/**
 * Created by kairat on 3/18/15.
 */
public class ReplyPresenterImpl implements ReplyPresenter, ReplyModelListener {

    private static final int GALLERY_RESULT = 1;
    private static final int CAMERA_RESULT = 2;
    private static final String albumName = "SkadateX";
    private static final String tmpFileName = "mailbox_compose_camera_attachment";
    private static final String tmpFileExt = ".jpg";

    private String mCurrentPhotoPath;

    private ReplyView mReplyView;
    private ReplyModel mReplyModel;

    public ReplyPresenterImpl(ReplyView replyView) {
        mReplyView = replyView;
        mReplyModel = new ReplyModelImpl();
    }

    @Override
    public void attachmentClick() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(mReplyView.getContext());
        myAlertDialog.setTitle(R.string.mailbox_attachment_dialog_title);

        myAlertDialog.setPositiveButton(R.string.mailbox_attachment_dialog_gallery, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
                galleryIntent.setType("file/*");
                galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                galleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                galleryIntent.putExtra("return-data", true);
                mReplyView.startActivity(Intent.createChooser(galleryIntent, mReplyView.getContext().getString(R.string.mailbox_attachment_dialog_select_app)), GALLERY_RESULT);
            }
        });

        myAlertDialog.setNegativeButton(R.string.mailbox_attachment_dialog_camera, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                try {
                    File f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    mReplyView.startActivity(cameraIntent, CAMERA_RESULT);
                } catch ( IOException e ) {
                    e.printStackTrace();
                    mReplyView.failCreateDirectory();
                }
            }
        });
        myAlertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( resultCode != Activity.RESULT_OK ) {
            return;
        }

        File file = null;
        String path = null;
        String type = null;

        switch ( requestCode ) {
            case GALLERY_RESULT:
                if ( data != null && data.getData() != null ) {
                    Uri uri = data.getData();
                    path = uri.getPath();
                    type = data.getType();
                    file = new File(path);

                    if ( !file.exists() ) {
                        Cursor cursor = mReplyView.getCursor(uri);
                        cursor.moveToFirst();

                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        path = cursor.getString(columnIndex);
                        file = new File(path);
                    }

                    if ( file.exists() && (type == null || type.isEmpty()) ) {
                        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(path));
                    }

                }

                break;
            case CAMERA_RESULT:
                path = mCurrentPhotoPath;
                type = "image/jpeg";
                file = new File(path);
                break;
            default: return;
        }

        if ( file == null || type == null || !file.exists() ) {
            mReplyView.fileNotFound();
        } else {
            int slotId = mReplyModel.attachAttachment(mReplyView.getUid(), type, path, this);
            mReplyView.prepareSlotForAttachment(slotId);
        }
    }

    @Override
    public void deleteAttachment(MailImage.Message.Attachment attachment) {
        mReplyModel.deleteAttachment(attachment, this);
    }

    @Override
    public void sendMessage(ReplyInterface compose) {
        mReplyView.showPleaseWaite();
        mReplyModel.sendMessage(compose, this);
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);

            if ( !storageDir.mkdirs() ) {
                if ( !storageDir.exists() ) {
                    mReplyView.failCreateDirectory();

                    return null;
                }
            }

        } else {
            mReplyView.failPermissionDirectory();
        }

        return storageDir;
    }

    private File createImageFile() throws IOException {
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(tmpFileName, tmpFileExt, albumF);
        return imageF;
    }

    private File setUpPhotoFile() throws IOException {
        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private JsonObject prepareData(Bundle bundle) {
        JsonObject json = SkApi.processResult(bundle);

        if (json == null || !json.has("result")) {
            return null;
        }

        JsonObject tmpObject = json.getAsJsonObject("result");

        if (tmpObject.has("error") && tmpObject.getAsJsonPrimitive("error").getAsBoolean()) {
            mReplyView.onResponseError(tmpObject.get("message").getAsString());

            return null;
        }

        return json.getAsJsonObject("result");
    }

    @Override
    public void onAttachAttachment(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject json = prepareData(bundle);

        if (json == null) {
            mReplyView.destroySlotForAttachment(requestId);

            return;
        }

        MailImage.Message.Attachment attachment = new MailImage.Message.Attachment();
        attachment.setId(json.get("id").getAsInt());
        attachment.setDownloadUrl(json.get("downloadUrl").getAsString());
        attachment.setFileSize(json.get("size").getAsInt());
        mReplyView.updateSlotForAttachment(requestId, attachment);
    }

    @Override
    public void onDeleteAttachment(MailImage.Message.Attachment attachment, int requestId, Intent requestIntent, int resultCode, Bundle data) {

    }

    @Override
    public void onMailSend(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject json = prepareData(bundle);

        if (json == null) {
            return;
        }

        AuthorizationInfo.getInstance().updateAuthorizationInfo(json);
        mReplyView.hidePleaseWaite();
        mReplyView.onSendMessage(new Gson().fromJson(json.get("message"), ConversationHistory.Messages.Message.class));
    }
}
