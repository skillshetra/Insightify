package com.example.insightify;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
public class Login extends AppCompatActivity {
    EditText etUsername, etPassword;
    Button btnLogin;
    // Firebase Realtime Database reference
    private DatabaseReference mDatabase;
    // SharedPreferences session constants
    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "session_pref";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Initialize Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        // If already logged in, go to MainActivity
        if (isLoggedIn) {
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
            return;
        }
        findViewById(R.id.registerActivityOpen).setOnClickListener(v -> {
            Intent intent = new Intent(this, Register.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            // Check if fields are empty
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            // Firebase login using Realtime Database
            loginUser(username, password);
        });
    }
    // Firebase login function
    private void loginUser(String username, String password) {
        mDatabase.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // If a user with this username exists
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                User user = userSnapshot.getValue(User.class);
                                // Check if password matches
                                if (user != null && user.getPassword().equals(password)) {
                                    // Save login status
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(KEY_IS_LOGGED_IN, true);
                                    editor.putString("userId", user.getUserId());
                                    editor.apply();
                                    // Navigate to MainActivity
                                    startActivity(new Intent(Login.this, MainActivity.class));
                                    finish();
                                    return;
                                }
                            }
                        }
                        // If login fails
                        Toast.makeText(Login.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle any database errors
                        Toast.makeText(Login.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public static class User {
        private String userId;
        private String name;
        private String username;
        private String password;
        public User() {}
        public User(String userId, String name, String username, String password) {
            this.userId = userId;
            this.name = name;
            this.username = username;
            this.password = password;
        }
        public String getUserId() {return userId;}
        public void setUserId(String userId) {this.userId = userId;}
        public String getName() {return name;}
        public void setName(String name) {this.name = name;}
        public String getUsername() {return username;}
        public void setUsername(String username) {this.username = username;}
        public String getPassword() {return password;}
        public void setPassword(String password) {this.password = password;}
    }
}