package com.example.insightify;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "session_pref";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Session check
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        if (!isLoggedIn) {
            // If not logged in, go back to Login screen
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
            return;
        }
        // Logout button functionality
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_IS_LOGGED_IN, false);
            editor.apply();
            // Go back to Login screen
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        });
        // Initialize buttons
        Button button1 = findViewById(R.id.mainButton1);
        Button button2 = findViewById(R.id.mainButton2);
        Button button3 = findViewById(R.id.mainButton3);
        // Set onClick for Button 1
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TextGeneration Activity
                Intent intent = new Intent(MainActivity.this, TextGeneration.class);
                startActivity(intent);
            }
        });
        // Set onClick for Button 2
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TextGeneration Activity
                Intent intent = new Intent(MainActivity.this, CodeGeneration.class);
                startActivity(intent);
            }
        });
        // Set onClick for Button 3
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TextGeneration Activity
                Intent intent = new Intent(MainActivity.this, ImageGeneration.class);
                startActivity(intent);
            }
        });
    }
}