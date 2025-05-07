package com.example.insightify;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import io.noties.markwon.Markwon;
public class CodeGeneration extends AppCompatActivity {
    TextView codeGenerationTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_code_generation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.sendButtonCodeGeneration).setOnClickListener(v -> {
            EditText input = findViewById(R.id.messageInputCodeGeneration);
            codeGenerationTextView = findViewById(R.id.codeInsertTextView);
            String response = getResponse("You are a code generator reply in markdown and only reply code with full of comments and well explained. " + input.getText().toString().trim());
            input.setText("");
            Markwon markwon = Markwon.create(this);
            markwon.setMarkdown(codeGenerationTextView, response);
        });
    }
    public String getResponse(String userMessage) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        OkHttpClient client = new OkHttpClient();
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "gpt-3.5-turbo");
            JSONArray messages = new JSONArray();
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.put(userMsg);
            jsonBody.put("messages", messages);
            jsonBody.put("temperature", 0.7);
            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json")
            );
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer YOUR-API-KEY")
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                JSONArray choices = jsonObject.getJSONArray("choices");
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                return message.getString("content").trim();
            } else {
                return "Error: " + response.code() + " - " + response.message();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception: " + e.getMessage();
        }
    }
}