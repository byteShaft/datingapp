package com.mydatingapp.ui.mailbox;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mydatingapp.ui.mailbox.chat.model.ConversationHistory;
import com.mydatingapp.ui.mailbox.conversation_list.model.ConversationList;
import com.mydatingapp.ui.mailbox.mail.image.MailImage;

import java.lang.reflect.Type;

/**
 * Created by kairat on 4/21/15.
 */
public class DeserializerHelper {
    private static DeserializerHelper mInstance;

    public static DeserializerHelper getInstance() {
        if (mInstance == null) {
            synchronized (DeserializerHelper.class) {
                mInstance = new DeserializerHelper();
            }
        }

        return mInstance;
    }

    public ConversationList getConversationList(JsonObject object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ConversationList.class, new ConversationDeserializer());
        Gson gson = gsonBuilder.create();

        return gson.fromJson(object, ConversationList.class);
    }

    public ConversationHistory getConversationHistory(JsonObject object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ConversationHistory.class, new ConversationHistoryDeserializer());
        Gson gson = gsonBuilder.create();

        return gson.fromJson(object, ConversationHistory.class);
    }

    public MailImage getMailImage(JsonObject object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MailImage.class, new MailDeserializer());

        return gsonBuilder.create().fromJson(object, MailImage.class);
    }

    private class ConversationDeserializer implements JsonDeserializer<ConversationList> {
        @Override
        public ConversationList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject tmpJsonObject = json.getAsJsonObject();
            JsonArray list;

            if (!tmpJsonObject.has("list") || !tmpJsonObject.get("list").isJsonArray() || (list = tmpJsonObject.getAsJsonArray("list")).size() == 0) {
                return null;
            }

            ConversationList conversationList = new ConversationList();
            Gson gson = new Gson();

            for (JsonElement jsonElement : list) {
                try {
                    ConversationList.ConversationItem item = gson.fromJson(jsonElement, ConversationList.ConversationItem.class);
                    conversationList.addOrUpdateConversationItem(item);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }

            return conversationList;
        }
    }

    private class ConversationHistoryDeserializer implements JsonDeserializer<ConversationHistory> {
        @Override
        public ConversationHistory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject tmpJsonObject = json.getAsJsonObject();
            JsonArray list;

            if (!tmpJsonObject.has("list") || !tmpJsonObject.get("list").isJsonArray() || (list = tmpJsonObject.getAsJsonArray("list")).size() == 0) {
                return null;
            }

            ConversationHistory conversationHistory = new ConversationHistory();
            Gson gson = new Gson();

            for (JsonElement jsonElement : list) {
                try {
                    JsonObject tmpMessage = jsonElement.getAsJsonObject();
                    String date = tmpMessage.get("dateLabel").getAsString();
                    ConversationHistory.DailyHistory dailyHistory = conversationHistory.getDailyHistoryByDate(date);

                    if (dailyHistory == null) {
                        dailyHistory = new ConversationHistory.DailyHistory(date);
                        conversationHistory.addHistory(dailyHistory);
                    }

                    ConversationHistory.Messages.Message message = gson.fromJson(jsonElement, ConversationHistory.Messages.Message.class);
                    dailyHistory.addMessage(message);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }

            return conversationHistory;
        }
    }

    private class MailDeserializer implements JsonDeserializer<MailImage> {
        @Override
        public MailImage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject tmpJsonObject = json.getAsJsonObject();
            JsonArray list;


            if (!tmpJsonObject.has("list") || !tmpJsonObject.get("list").isJsonArray() || (list = tmpJsonObject.getAsJsonArray("list")).size() == 0) {
                return null;
            }

            MailImage  mailImage = new MailImage();
            Gson gson = new Gson();

            for (JsonElement jsonElement : list) {
                try {
                    MailImage.Message message = gson.fromJson(jsonElement, MailImage.Message.class);
                    mailImage.addMessage(message);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }

            return mailImage;
        }
    }
}
