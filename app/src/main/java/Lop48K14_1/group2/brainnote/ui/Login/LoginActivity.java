package Lop48K14_1.group2.brainnote.ui.Login;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import Lop48K14_1.group2.brainnote.R;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        // Set up button click listeners
        CardView googleSignInButton = findViewById(R.id.googleSignInButton);
        CardView createAccountButton = findViewById(R.id.createAccountButton);
        CardView signInButton = findViewById(R.id.signInButton);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Google Sign In Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Create Account Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Sign In Clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
