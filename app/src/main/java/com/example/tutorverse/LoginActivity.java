package com.example.tutorverse;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    EditText edEmail, edPassword;
    Button btnLogin;
    TextView tvRegisterLink;

    FirebaseAuth auth;
    DatabaseReference dbUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        int paddingPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left + paddingPx,
                    systemBars.top + paddingPx,
                    systemBars.right + paddingPx,
                    systemBars.bottom + paddingPx
            );
            return insets;
        });

        initialize();
    }

    private void initialize() {
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        auth = FirebaseAuth.getInstance();
        dbUsers = FirebaseDatabase.getInstance().getReference("users");

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegisterLink.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(i);
        });
    }

    private void loginUser() {
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    String uid = result.getUser().getUid();

                    dbUsers.child(uid).get().addOnSuccessListener(snapshot -> {
                        if (!snapshot.exists()) {
                            goToStudentDashboard();
                            return;
                        }

                        String role = snapshot.child("role").getValue(String.class);
                        String normalizedRole = role == null ? "" : role.trim();

                        if (normalizedRole.equalsIgnoreCase("Tutor")) {
                            Toast.makeText(this, "Welcome tutor!", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(LoginActivity.this, TutorDashboardActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            goToStudentDashboard();
                        }

                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Login ok, but failed to load role", Toast.LENGTH_SHORT).show();
                        goToStudentDashboard();
                    });

                })
                .addOnFailureListener(e -> {
                    String msg = e.getMessage();
                    if (msg != null) {
                        if (msg.contains("password")) msg = "Incorrect password.";
                        else if (msg.contains("no user")) msg = "No account found with this email.";
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                });
    }

    private void goToStudentDashboard() {
        Intent i = new Intent(LoginActivity.this, StudentDashboardActivity.class);
        startActivity(i);
        finish();
    }
}