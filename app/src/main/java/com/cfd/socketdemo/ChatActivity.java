package com.cfd.socketdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.cfd.socketdemo.databinding.ActivityChatBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatActivity extends AppCompatActivity implements TextWatcher {

    private ActivityChatBinding binding;

    private String name;
    public static final String SERVER_PATH = "ws://websocket-echo.com";

    private WebSocket webSocket;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        name = getIntent().getStringExtra("name");
        initial();

    }

    private void initial() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                super.onOpen(webSocket, response);
                System.out.println("OK");
                runOnUiThread(()-> {
                    Toast.makeText(ChatActivity.this, "Connect", Toast.LENGTH_SHORT).show();
                    initialView();
                });
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                super.onMessage(webSocket, text);

                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        jsonObject.put("isSent", false);
                        adapter.addItem(jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                });
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                System.out.println(t.getMessage());
            }
        });
    }
    private void initialView() {
        binding.editText.addTextChangedListener(this);
        adapter = new MessageAdapter(getLayoutInflater());
        binding.rv.setLayoutManager(new LinearLayoutManager(this));
        binding.rv.setAdapter(adapter);

        binding.btnSend.setOnClickListener(v -> {
            System.out.println("Click");
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("name", name);
                jsonObject.put("message", binding.editText.getText().toString());

                webSocket.send(jsonObject.toString());

                jsonObject.put("isSent", true);
                adapter.addItem(jsonObject);
                resetMsgEdit();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        binding.img.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

            startActivityForResult(Intent.createChooser(intent, "Pick image"), 200);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == RESULT_OK) {

            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                sendImage(bitmap);
            } catch (FileNotFoundException | JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void sendImage(Bitmap bitmap) throws JSONException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

        String base64String = android.util.Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("image", base64String);

        webSocket.send(jsonObject.toString());

        jsonObject.put("isSent", true);
        adapter.addItem(jsonObject);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String string = s.toString().trim();
        if (string.isEmpty()) {
            resetMsgEdit();
        } else {
            binding.btnSend.setVisibility(View.VISIBLE);
            binding.img.setVisibility(View.INVISIBLE);
        }
    }

    private void resetMsgEdit() {
        binding.editText.removeTextChangedListener(this);
        binding.editText.setText("");
        binding.btnSend.setVisibility(View.INVISIBLE);
        binding.img.setVisibility(View.VISIBLE);

        binding.editText.addTextChangedListener(this);
    }
}