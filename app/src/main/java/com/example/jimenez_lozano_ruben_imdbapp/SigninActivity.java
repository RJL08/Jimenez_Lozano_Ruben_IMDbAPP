package com.example.jimenez_lozano_ruben_imdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);

        // Configuración de márgenes para el layout principal
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Agrega el ID del cliente
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Registrar el ActivityResultLauncher para manejar el resultado
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            // El inicio de sesión con Google fue exitoso
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                String email = account.getEmail(); // Obtén el correo del usuario
                                Toast.makeText(this, "Signed in as: " + email, Toast.LENGTH_SHORT).show();
                                Log.d("GoogleSignIn", "Correo: " + email);
                            }
                        } catch (ApiException e) {
                            // Maneja el error
                            Log.w("GoogleSignIn", "Google sign in failed", e);
                            Toast.makeText(this, "Sign-In Failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Resultado cancelado o inválido
                        Toast.makeText(this, "Sign-In Canceled", Toast.LENGTH_SHORT).show();
                    }
                });

        // Encuentra el botón SignInButton en el diseño
        SignInButton signInButton = findViewById(R.id.sign_in_button);

        // Cambia el texto del botón a "Sign in with Google"
        setGoogleSignInButtonText(signInButton, "Sign in with Google");

        // Configura el evento onClick para el botón
        signInButton.setOnClickListener(v -> signInWithGoogle());
    }

    /**
     * Cambia el texto del botón SignInButton.
     *
     * @param signInButton El botón SignInButton a modificar.
     * @param buttonText   El texto personalizado que deseas mostrar.
     */
    private void setGoogleSignInButtonText(SignInButton signInButton, String buttonText) {
        // Accede a los elementos hijos del SignInButton
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View view = signInButton.getChildAt(i);
            if (view instanceof TextView) {
                // Cambia el texto del TextView interno
                ((TextView) view).setText(buttonText);
                return;
            }
        }
    }

    /**
     * Inicia el flujo de inicio de sesión con Google.
     */
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent); // Usa el launcher en lugar de startActivityForResult
    }
}