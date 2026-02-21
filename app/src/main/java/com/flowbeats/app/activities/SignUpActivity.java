package com.flowbeats.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.flowbeats.app.R;
import com.flowbeats.app.database.AppDatabase;
import com.flowbeats.app.models.User;
import com.flowbeats.app.utils.SharedPreferenceManager;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {
    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private SharedPreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        prefManager = new SharedPreferenceManager(this);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> attemptSignup());
    }

    private void attemptSignup() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.error_passwords_dont_match, Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            User existingUser = db.userDao().getUserByEmail(email);
            if (existingUser != null) {
                runOnUiThread(
                        () -> Toast.makeText(SignUpActivity.this, "User already exists", Toast.LENGTH_SHORT).show());
            } else {
                User newUser = new User(email, name, password);
                db.userDao().insert(newUser);

                // Still keep login preference for session management
                prefManager.setLoggedIn(true);
                prefManager.saveUserData(name, email, password); // Optional: keep for easy access or remove if fully
                                                                 // relying on DB

                runOnUiThread(() -> {
                    Toast.makeText(SignUpActivity.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    finish();
                });
            }
        });
    }
}
