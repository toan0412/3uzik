package com.example.a3uzik;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.a3uzik.databinding.ActivityLoginBinding;
import com.example.a3uzik.databinding.ActivitySignUpBinding;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signInBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
        binding.signInWithGoogleBtn.setOnClickListener(v ->{
            Toast.makeText(
                    this,
                    "Chức năng này sẽ sớm xuất hiện XD",
                    Toast.LENGTH_SHORT
            ).show();
        });
        binding.signInWithFBBtn.setOnClickListener(v ->{
            Toast.makeText(
                    this,
                    "Chức năng này sẽ sớm xuất hiện XD",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}