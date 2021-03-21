package com.example.recipefinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView btn = findViewById(R.id.TextViewLogin);
        btn.setOnClickListener(
                view -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        TextView buton = findViewById(R.id.textViewSkip);
        buton.setOnClickListener(
                view -> startActivity(new Intent(RegisterActivity.this, MainActivity.class)));

    }
    }
