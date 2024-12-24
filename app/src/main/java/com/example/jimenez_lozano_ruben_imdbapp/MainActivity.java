package com.example.jimenez_lozano_ruben_imdbapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.jimenez_lozano_ruben_imdbapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {


    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar el layout principal
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener los datos del usuario pasados desde SigninActivity
        Intent intent = getIntent();
        String userName = intent.getStringExtra("user_name");
        String userEmail = intent.getStringExtra("user_email");
        String userPhotoUrl = intent.getStringExtra("user_photo");

        // Configurar el NavigationView
        NavigationView navigationView = binding.navView;
        // Acceder al encabezado del NavigationView
        View headerView = navigationView.getHeaderView(0);
        LinearLayout headerLayout = headerView.findViewById(R.id.header_container);

        // Obtener la altura del notch (si existe)
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        // Aplicar margen superior dinámico
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) headerLayout.getLayoutParams();
        params.topMargin = statusBarHeight + 32; // Ajuste adicional
        headerLayout.setLayoutParams(params);

        // Inicializar las vistas del encabezado (nav_header_main)
        ImageView profileImageView = headerView.findViewById(R.id.imageView);
        TextView nameTextView = headerView.findViewById(R.id.user_name);
        TextView emailTextView = headerView.findViewById(R.id.user_email);
        Button logoutButton = headerView.findViewById(R.id.logout_button);

        // Mostrar los datos del usuario en las vistas
        if (userName != null) {
            nameTextView.setText(userName);
        }
        if (userEmail != null) {
            emailTextView.setText(userEmail);
            Toast.makeText(this, "Welcome: " + userEmail, Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "User Email: " + userEmail);
        }

        if (userPhotoUrl != null) {
            // Usar Glide para cargar la imagen del usuario
            Glide.with(this)
                    .load(userPhotoUrl)
                    .placeholder(R.drawable.ic_launcher_background) // Imagen por defecto
                    .error(R.drawable.ic_launcher_foreground) // Imagen de error
                    .into(profileImageView);
        }
        // Configurar el botón de logout
        logoutButton.setOnClickListener(v -> {
            // Limpiar el estado de inicio de sesión en SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit();
            editor.clear(); // Elimina todas las preferencias
            editor.apply(); // Confirma los cambios

            // Cerrar sesión con Google
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                    .addOnCompleteListener(task -> {
                        // Regresar a SigninActivity
                        Intent signOutIntent = new Intent(MainActivity.this, SigninActivity.class);
                        signOutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpia la pila de actividades
                        startActivity(signOutIntent);
                        finish(); // Finaliza MainActivity
                    });
        });

        // Configurar la barra de herramientas
        setSupportActionBar(binding.appBarMain.toolbar);

        // Configurar el DrawerLayout y Navigation Controller
        DrawerLayout drawer = binding.drawerLayout;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Manejar el clic en el menú
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Navegar al fragmento Top 10
                navController.navigate(R.id.nav_home);
                drawer.closeDrawers(); // Cerrar el menú lateral
                return true;
            } else if (id == R.id.nav_slideshow) {
                // Navegar al fragmento Buscar Películas
                navController.navigate(R.id.nav_slideshow);
                drawer.closeDrawers(); // Cerrar el menú lateral
                return true;
            } else if (id == R.id.nav_gallery) {
                // Navegar al fragmento Favoritos
                navController.navigate(R.id.nav_gallery);
                drawer.closeDrawers(); // Cerrar el menú lateral
                return true;
            }

            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}