package com.example.jimenez_lozano_ruben_imdbapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.common.SignInButton;

public class SigninActivity extends AppCompatActivity {

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

        // Encuentra el botón SignInButton en el diseño
        SignInButton signInButton = findViewById(R.id.sign_in_button);

        // Cambia el texto del botón a "Sign in with Google"
        setGoogleSignInButtonText(signInButton, "Sign in with Google");

        // Configura el evento onClick para el botón
        signInButton.setOnClickListener(v -> {
            // Llama al método de inicio de sesión
            signInWithGoogle();
        });
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
     * Método que implementa la lógica de inicio de sesión con Google.
     */
    private void signInWithGoogle() {
        // Aquí implementa tu lógica de inicio de sesión con Google
        Toast.makeText(this, "Iniciando sesión con Google...", Toast.LENGTH_SHORT).show();
    }
}