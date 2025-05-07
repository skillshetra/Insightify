package com.example.insightify;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class ImageGeneration extends AppCompatActivity {
    private static final String API_KEY = "YOUR-API-KEY";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_generation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        findViewById(R.id.sendButtonImageGeneration).setOnClickListener(v -> {
            EditText textInputField = findViewById(R.id.messageInputImageGeneration);
            ImageView imageView = findViewById(R.id.imageView);
            String prompt = textInputField.getText().toString().trim();
            textInputField.setText("");
            String imageBase64Url = generateImage(prompt);
            if (imageBase64Url.startsWith("data:image")) {
                Glide.with(this).load(imageBase64Url).into(imageView);
            }
            Toast.makeText(this, imageBase64Url.length() > 100 ? "Image generated" : imageBase64Url, Toast.LENGTH_SHORT).show();
        });
    }
    public String generateImage(String userPrompt) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).build();
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "dall-e-3");
            jsonBody.put("prompt", userPrompt);
            jsonBody.put("response_format", "b64_json");
            jsonBody.put("n", 1);
            RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder().url("https://api.openai.com/v1/images/generations").header("Authorization", "Bearer " + API_KEY).post(body).build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                JSONArray data = jsonObject.getJSONArray("data");
                String base64 = data.getJSONObject(0).getString("b64_json");
                return "data:image/png;base64," + base64;
            } else {
                return "Error: " + response.code() + " - " + response.message();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception: " + e.getMessage();
        }
    }
}