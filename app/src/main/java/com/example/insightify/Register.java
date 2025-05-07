package com.example.insightify;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
public class Register extends AppCompatActivity {
    private EditText etName, etUsername, etPassword;
    private DatabaseReference usersDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        usersDatabase = FirebaseDatabase.getInstance().getReference("users");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        findViewById(R.id.btnRegister).setOnClickListener(v -> {handleRegister();});
    }
    private void handleRegister() {
        String name = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
        } else {
            checkUsernameExists(username);
        }
    }
    private void checkUsernameExists(String username) {
        usersDatabase.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Username already exists
                    Toast.makeText(Register.this, "Username already exists. Please choose another.", Toast.LENGTH_SHORT).show();
                } else {
                    createUser(username);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Register.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void createUser(String username) {
        String name = etName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String userId = usersDatabase.push().getKey();
        User newUser = new User(userId, name, username, password);
        usersDatabase.child(userId).setValue(newUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Register.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        SharedPreferences sharedPreferences = getSharedPreferences("session_pref", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("is_logged_in", true);
                        editor.putString("userId", userId);
                        editor.apply();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(Register.this, "Registration failed! Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public static class User {
        private String userId;
        private String name;
        private String username;
        private String password;
        public User() {} // Default constructor
        public User(String userId, String name, String username, String password) {
            this.userId = userId;
            this.name = name;
            this.username = username;
            this.password = password;
        }
        public String getUserId() {return userId;}
        public void setUserId(String userId) {this.userId = userId;}
        public String getName() {return name;
        }
        public void setName(String name) {this.name = name;}
        public String getUsername() {return username;}
        public void setUsername(String username) {this.username = username;}
        public String getPassword() {return password;}
        public void setPassword(String password) {this.password = password;}
    }
}