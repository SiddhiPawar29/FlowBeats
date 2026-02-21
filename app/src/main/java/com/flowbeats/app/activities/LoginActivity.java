package com.flowbeats.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.flowbeats.app.R;
import com.flowbeats.app.utils.SharedPreferenceManager;
import com.google.android.material.textfield.TextInputEditText;

import com.flowbeats.app.database.AppDatabase;
import com.flowbeats.app.models.User;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignup;
    private SharedPreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefManager = new SharedPreferenceManager(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            User user = db.userDao().login(email, password);

            runOnUiThread(() -> {
                if (user != null) {
                    prefManager.setLoggedIn(true);
                    prefManager.saveUserData(user.getFullName(), user.getEmail(), user.getPassword());
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.error_invalid_credentials, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
