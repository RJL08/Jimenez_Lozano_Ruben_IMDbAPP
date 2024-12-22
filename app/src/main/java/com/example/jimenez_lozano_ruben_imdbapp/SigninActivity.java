package com.example.jimenez_lozano_ruben_imdbapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class SigninActivity extends AppCompatActivity {
    private GoogleSignInClient googleSignInClient; // Cliente de Google Sign-In
    private ActivityResultLauncher<Intent> signInLauncher; // Launcher para el resultado del SignIn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Comprobar si el usuario ya está registrado
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Navegar directamente al MainActivity
            Intent intent = new Intent(SigninActivity.this, MainActivity.class);
            intent.putExtra("user_name", prefs.getString("userName", ""));
            intent.putExtra("user_email", prefs.getString("userEmail", ""));
            intent.putExtra("user_photo", prefs.getString("userPhoto", "https://lh3.googleusercontent.com/a/default-user"));
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_signin);

        // Configurar el Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sign In");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false); // Deshabilitar botón de volver
        }

        // Configurar el botón de Google Sign-In
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        setGoogleSignInButtonText(signInButton, "Sign in with Google");

        // Configurar GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // ID del cliente de Firebase
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configurar el ActivityResultLauncher
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                // Guardar estado de inicio de sesión
                                SharedPreferences.Editor editor = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit();
                                editor.putBoolean("isLoggedIn", true);
                                editor.putString("userName", account.getDisplayName());
                                editor.putString("userEmail", account.getEmail());
                                editor.putString("userPhoto", account.getPhotoUrl() != null
                                        ? account.getPhotoUrl().toString()
                                        : "https://lh3.googleusercontent.com/a/default-user");
                                editor.apply();

                                // Navegar a MainActivity
                                Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                                intent.putExtra("user_name", account.getDisplayName());
                                intent.putExtra("user_email", account.getEmail());
                                intent.putExtra("user_photo", account.getPhotoUrl() != null
                                        ? account.getPhotoUrl().toString()
                                        : "https://lh3.googleusercontent.com/a/default-user");
                                startActivity(intent);
                                finish();
                            }
                        } catch (ApiException e) {
                            Log.w("GoogleSignIn", "Sign-In Failed", e);
                            Toast.makeText(this, "Sign-In Failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Sign-In Canceled", Toast.LENGTH_SHORT).show();
                    }
                });

        // Configurar el evento del botón de inicio de sesión
        signInButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });
    }

    // Cambia el texto del botón de Google Sign-In
    private void setGoogleSignInButtonText(SignInButton signInButton, String buttonText) {
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            if (signInButton.getChildAt(i) instanceof TextView) {
                ((TextView) signInButton.getChildAt(i)).setText(buttonText);
                return;
            }
        }
    }

    /**
     * Navegar a MainActivity pasando el correo electrónico.
     */
    private void navegarActivityMain(GoogleSignInAccount account) {
        Intent intent = new Intent(SigninActivity.this, MainActivity.class);
        intent.putExtra("user_name", account.getDisplayName());
        intent.putExtra("user_email", account.getEmail());
        startActivity(intent);
        finish(); // Finalizar SigninActivity
    }
}