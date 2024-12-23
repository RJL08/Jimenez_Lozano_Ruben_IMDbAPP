package com.example.jimenez_lozano_ruben_imdbapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.jimenez_lozano_ruben_imdbapp.api.IMDBApiService;
import com.example.jimenez_lozano_ruben_imdbapp.ui.MovieOverviewResponse;
import retrofit2.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieDetailsActivity extends AppCompatActivity {
    private ImageView imageMovie;
    private TextView titleMovie, releaseDate, rating, description;
    private ActivityResultLauncher<Intent> contactPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Inicializar vistas
        imageMovie = findViewById(R.id.image_movie);
        titleMovie = findViewById(R.id.title_movie);
        releaseDate = findViewById(R.id.release_date_view);
        rating = findViewById(R.id.rating_view);
        description = findViewById(R.id.description_view);

        // Configurar el lanzador para elegir contacto
        contactPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri contactUri = result.getData().getData();
                        if (contactUri != null) {
                            retrievePhoneNumber(contactUri);
                        }
                    } else {
                        Toast.makeText(this, "No se seleccionó ningún contacto", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurar título y habilitar el botón de retroceso si es necesario
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detalles de la Película");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Obtener datos desde el Intent
        Intent intent = getIntent();
        String movieId = intent.getStringExtra("movie_id");
        String movieTitle = intent.getStringExtra("movie_title");
        String movieImage = intent.getStringExtra("movie_image");

        Button btnSendSms = findViewById(R.id.btn_send_sms);
        btnSendSms.setOnClickListener(v -> checkPermissionsAndSendSms());

        // Mostrar datos iniciales (título e imagen)
        titleMovie.setText(movieTitle != null ? movieTitle : "Título no disponible");
        Glide.with(this)
                .load(movieImage != null ? movieImage : R.drawable.esperando) // Imagen por defecto si no hay URL
                .placeholder(R.drawable.esperando)
                .error(R.drawable.esperando)
                .into(imageMovie);

        // Verificar si hay un ID de película para buscar más detalles
        if (movieId != null) {
            fetchMovieDetails(movieId);
        } else {
            Toast.makeText(this, "Error: No se encontró información de la película", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Finalizar la actividad y regresar a la anterior
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchMovieDetails(String movieId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://imdb-com.p.rapidapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        IMDBApiService apiService = retrofit.create(IMDBApiService.class);

        Call<MovieOverviewResponse> call = apiService.getMovieOverview(
                movieId,
                "3ef3f2c2a3msh17da27eb24608e1p12db6bjsn62d2b74752ff",
                "imdb-com.p.rapidapi.com"
        );

        call.enqueue(new Callback<MovieOverviewResponse>() {
            @Override
            public void onResponse(Call<MovieOverviewResponse> call, Response<MovieOverviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieOverviewResponse movie = response.body();

                    String releaseDateText = (movie.data.title.releaseDate != null) ?
                            movie.data.title.releaseDate.day + "/" +
                                    movie.data.title.releaseDate.month + "/" +
                                    movie.data.title.releaseDate.year :
                            "Fecha no disponible";

                    String ratingText = (movie.data.title.ratingsSummary != null &&
                            movie.data.title.ratingsSummary.aggregateRating != null) ?
                            String.valueOf(movie.data.title.ratingsSummary.aggregateRating) :
                            "No hay valoraciones disponibles";

                    String descriptionText = (movie.data.title.plot != null &&
                            movie.data.title.plot.plotText != null) ?
                            movie.data.title.plot.plotText.plainText :
                            "Sin descripción";

                    // Log para depuración
                    Log.d("MOVIE_DETAILS", "Release Date: " + releaseDateText);
                    Log.d("MOVIE_DETAILS", "Rating: " + ratingText);
                    Log.d("MOVIE_DETAILS", "Description: " + descriptionText);

                    // Actualizar la UI
                    runOnUiThread(() -> {
                        releaseDate.setText("Release Date: " + releaseDateText);
                        rating.setText("Rating: " + ratingText);
                        description.setText(descriptionText);
                    });
                } else {
                    Log.e("API_ERROR", "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieOverviewResponse> call, Throwable t) {
                Log.e("API_ERROR", "Error en la llamada: " + t.getMessage());
            }
        });
    }

    private void checkPermissionsAndSendSms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            // Solicitar permisos
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS}, 100);
        } else {
            // Continuar con el proceso de envío
            chooseContactAndSendSms();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                chooseContactAndSendSms();
            } else {
                Toast.makeText(this, "Permisos denegados. No se puede enviar SMS.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void chooseContactAndSendSms() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, 200);
    }

    private void retrievePhoneNumber(Uri contactUri) {
        String phoneNumber = null;

        // Consultar el contenido del contacto
        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            // Obtener el ID del contacto
            String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            String hasPhone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if (hasPhone.equalsIgnoreCase("1")) {
                Cursor phones = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id},
                        null
                );

                if (phones != null && phones.moveToFirst()) {
                    phoneNumber = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phones.close();
                }
            }
            cursor.close();
        }

        if (phoneNumber != null) {
            sendSms(phoneNumber);
        } else {
            Toast.makeText(this, "El contacto no tiene número de teléfono", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSms(String phoneNumber) {
        SmsManager smsManager = SmsManager.getDefault();
        String message = "Te recomiendo la película: " + titleMovie.getText().toString() +
                "\n" + description.getText().toString() +
                "\n" + releaseDate.getText().toString() +
                "\n" + rating.getText().toString();

        try {
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS enviado a " + phoneNumber, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al enviar SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onContactSelected(String phoneNumber) {
        String movieTitle = titleMovie.getText().toString();
        String movieRating = rating.getText().toString();

        // Crear el mensaje para enviar
        String message = "Esta película te gustará: " + movieTitle + "\n" + movieRating;

        // Crear un Intent para abrir la aplicación de mensajes con el número del contacto y el mensaje
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + phoneNumber)); // Establecer el número de teléfono
        smsIntent.putExtra("sms_body", message); // Establecer el cuerpo del mensaje

        try {
            startActivity(smsIntent); // Abrir la aplicación de mensajes
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No se encontró una aplicación de mensajería", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();

            // Consultar el número de teléfono del contacto seleccionado
            Cursor cursor = getContentResolver().query(contactUri,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phoneNumber = cursor.getString(numberIndex);
                cursor.close();

                // Abrir la aplicación de mensajería con el contacto seleccionado
                openMessagingApp(phoneNumber);
            } else {
                Toast.makeText(this, "No se pudo obtener el número del contacto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openMessagingApp(String phoneNumber) {
        String movieTitle = titleMovie.getText().toString();
        String movieRating = rating.getText().toString();

        // Crear el mensaje para enviar
        String message = "Esta película te gustará: " + movieTitle + "\n" + movieRating;

        // Crear un Intent para abrir la aplicación de mensajes
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + phoneNumber)); // Establecer el número de teléfono
        smsIntent.putExtra("sms_body", message); // Establecer el cuerpo del mensaje

        try {
            startActivity(smsIntent); // Abrir la aplicación de mensajes
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No se encontró una aplicación de mensajería", Toast.LENGTH_SHORT).show();
        }
    }
}
