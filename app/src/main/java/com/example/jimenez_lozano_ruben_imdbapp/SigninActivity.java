package com.example.jimenez_lozano_ruben_imdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
;

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
    private ActivityResultLauncher<Intent> signInLauncher; // Launcher para el inicio de sesión con Google

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        SignInButton signInButton = findViewById(R.id.sign_in_button);

        // Cambiar el texto del botón
        setGoogleSignInButtonText(signInButton, "Sign in with Google");

        // Configurar el Toolbar
        Toolbar toolbar2 = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar2);


        // Establecer el texto para el Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sign In");
        }


        // Configurar GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Agrega el ID del cliente
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configurar ActivityResultLauncher para manejar el resultado
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            // Inicio de sesión exitoso
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                // Obtener el correo electrónico del usuario
                                String name = account.getDisplayName();
                                String email = account.getEmail();

                                // Navegar a MainActivity con los datos del usuario
                                Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                                intent.putExtra("user_name", account.getDisplayName());
                                intent.putExtra("user_email", account.getEmail());
                                if (account.getPhotoUrl() != null) {
                                    // Pasar la URL de la foto de perfil
                                    intent.putExtra("user_photo", account.getPhotoUrl().toString());
                                } else {
                                    // Si no hay foto de perfil, genera la URL predeterminada
                                    String defaultPhotoUrl = "https://lh3.googleusercontent.com/a/default-user"; // URL de Google para usuarios sin foto
                                    intent.putExtra("user_photo", defaultPhotoUrl);
                                }
                                startActivity(intent);
                                finish();

                                // Mostrar mensaje de éxito y navegar a MainActivity
                                Toast.makeText(this, "Signed in as: " + email, Toast.LENGTH_SHORT).show();
                                Log.d("GoogleSignIn", "Correo: " + email);
                                navegarActivityMain( account);
                            }
                        } catch (ApiException e) {
                            // Manejo de errores
                            Log.w("GoogleSignIn", "Google sign in failed", e);
                            Toast.makeText(this, "Sign-In Failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Resultado cancelado o inválido
                        Toast.makeText(this, "Sign-In Canceled", Toast.LENGTH_SHORT).show();
                    }
                });

        // Configurar botón de inicio de sesión
        findViewById(R.id.sign_in_button).setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });
    }

    /**
     * Cambia el texto del botón SignInButton.
     */
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