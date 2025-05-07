package com.example.insightify;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class TextGeneration extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private QuestionAdapter questionAdapter;
    private List<Question> questionList;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_generation);
        databaseReference = FirebaseDatabase.getInstance().getReference("questions");
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionList = new ArrayList<>();
        questionAdapter = new QuestionAdapter(questionList);
        chatRecyclerView.setAdapter(questionAdapter);
        fetchQuestionsFromFirebase();
        Button sendButton = findViewById(R.id.sendButtonTextGeneration);
        sendButton.setOnClickListener(view -> {
            EditText input = findViewById(R.id.messageInputTextGeneration);
            String questionText = input.getText().toString().trim();
            input.setText("");
            if (!questionText.isEmpty()) {
                String userId = getSharedPreferences("session_pref", MODE_PRIVATE).getString("userId", getSharedPreferences("session_pref", MODE_PRIVATE).getString("userId", null));
                Question question = new Question(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()), questionText, userId, "");
                textGenerationStart(question);
            }
        });
    }
    private void fetchQuestionsFromFirebase() {
        // Get the current user's ID from SharedPreferences
        String currentUserId = getSharedPreferences("session_pref", MODE_PRIVATE).getString("userId", null);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                questionList.clear();
                for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                    Question question = questionSnapshot.getValue(Question.class);
                    if (question != null && question.getUserId() != null && question.getUserId().equals(currentUserId)) {
                        questionList.add(question);
                    }
                }
                questionAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TextGeneration.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void textGenerationStart(Question question) {
        new Thread(() -> {
            String response = getResponse(question.getQuestionText());
            question.setAnswer(response);
            databaseReference.child(question.getQuestionId()).setValue(question);
            runOnUiThread(this::fetchQuestionsFromFirebase);
        }).start();
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
    // RecyclerView Adapter
    public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
        private final List<Question> questionList;
        public QuestionAdapter(List<Question> questionList) {
            this.questionList = questionList;
        }
        @Override
        public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
            return new QuestionViewHolder(view);
        }
        @Override
        public void onBindViewHolder(QuestionViewHolder holder, int position) {
            Question question = questionList.get(position);
            holder.questionText.setText("Q: " + question.getQuestionText() + "\nA: " + question.getAnswer());
        }
        @Override
        public int getItemCount() {
            return questionList.size();
        }
        public class QuestionViewHolder extends RecyclerView.ViewHolder {
            TextView questionText;
            public QuestionViewHolder(View itemView) {
                super(itemView);
                questionText = itemView.findViewById(R.id.questionText);
            }
        }
    }
    // ✅ Embedded Question model class
    public static class Question {
        private String questionId;
        private String questionText;
        private String userId;
        private String answer;
        // Required for Firebase
        public Question() {}
        public Question(String questionId, String questionText, String userId, String answer) {
            this.questionId = questionId;
            this.questionText = questionText;
            this.userId = userId;
            this.answer = answer;
        }
        public String getQuestionId() { return questionId; }
        public void setQuestionId(String questionId) { this.questionId = questionId; }
        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }
}