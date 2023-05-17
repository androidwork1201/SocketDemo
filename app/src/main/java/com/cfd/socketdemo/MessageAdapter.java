package com.cfd.socketdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cfd.socketdemo.databinding.LayoutReceivedMessageBinding;
import com.cfd.socketdemo.databinding.LayoutReceivedPhotoBinding;
import com.cfd.socketdemo.databinding.LayoutSendMessageBinding;
import com.cfd.socketdemo.databinding.LayoutSendPhotoBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter {

    private final int TYPE_MESSAGE_SENT = 0;
    private final int TYPE_MESSAGE_RECEIVED = 1;
    private final int TYPE_IMAGE_SENT = 2;
    private final int TYPE_IMAGE_RECEIVED = 3;

    private LayoutInflater inflater;
    private List<JSONObject> messages = new ArrayList<>();

    public MessageAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        switch (viewType) {
            case TYPE_MESSAGE_SENT: {
                view = inflater.inflate(R.layout.layout_send_message, parent, false);
                return new SentMsgHolder(view);
            }
            case TYPE_IMAGE_SENT: {
                view = inflater.inflate(R.layout.layout_send_photo, parent, false);
                return new SentImageHolder(view);
            }
            case TYPE_MESSAGE_RECEIVED: {
               view = inflater.inflate(R.layout.layout_received_message, parent, false);
               return new ReceivedMsgHolder(view);
            }
            case TYPE_IMAGE_RECEIVED: {
                view = inflater.inflate(R.layout.layout_received_photo, parent, false);
                return new ReceivedImageHolder(view);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        JSONObject message = messages.get(position);

        try {
            if (message.getBoolean("isSent")) {
                if (message.has("message")) {
                    SentMsgHolder sentMsgHolder = (SentMsgHolder) holder;
                    sentMsgHolder.messageText.setText(message.getString("message"));
                } else {
                    SentImageHolder sentImageHolder = (SentImageHolder) holder;
                    Bitmap bitmap = getBitmapFromString(message.getString("image"));
                    sentImageHolder.imageView.setImageBitmap(bitmap);
                }
            } else {
                if (message.has("message")) {
                    ReceivedMsgHolder receivedMsgHolder = (ReceivedMsgHolder) holder;
                    receivedMsgHolder.nameTxt.setText(message.getString("name"));
                    receivedMsgHolder.messageTxt.setText(message.getString("message"));
                } else {
                    ReceivedImageHolder receivedImageHolder = (ReceivedImageHolder) holder;
                    receivedImageHolder.nameTxt.setText(message.getString("name"));

                    Bitmap bitmap = getBitmapFromString(message.getString("image"));
                    receivedImageHolder.imageView.setImageBitmap(bitmap);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmapFromString(String image) {
        byte[] bytes = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public void addItem(JSONObject jsonObject) {
        messages.add(jsonObject);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {

        JSONObject message  = messages.get(position);

        try {
            if (message.getBoolean("isSent")) {
                if (message.has("message")) {
                    return TYPE_MESSAGE_SENT;
                } else {
                    return TYPE_IMAGE_SENT;
                }
            } else {
                if(message.has("message")){
                    return TYPE_MESSAGE_RECEIVED;
                }else{
                    return TYPE_IMAGE_RECEIVED;
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static class SentMsgHolder extends RecyclerView.ViewHolder {

        TextView messageText;

        public SentMsgHolder(@NonNull  View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.sendTxt);
        }
    }

    private static class SentImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public SentImageHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.sendImage);
        }
    }

    private static class ReceivedMsgHolder extends RecyclerView.ViewHolder {

        TextView nameTxt, messageTxt;

        public ReceivedMsgHolder(@NonNull View itemView) {
            super(itemView);
            nameTxt = itemView.findViewById(R.id.nameTxt);
            messageTxt = itemView.findViewById(R.id.receivedTxt);
        }
    }

    private static class ReceivedImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView nameTxt;

        public ReceivedImageHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.receivedImage);
            nameTxt = itemView.findViewById(R.id.nameTxt);
        }
    }

}
