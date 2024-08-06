package com.example.ejercicio.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ejercicio.R;
import com.example.ejercicio.models.ApiResponse;
import com.example.ejercicio.models.User;
import com.example.ejercicio.network.ApiService;
import com.example.ejercicio.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Collections;
public class UserActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText etUserId, etNombre, etEmail, etPassword;
    private Button btnCreateUser, btnReadUser, btnUpdateUser, btnDeleteUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        etUserId = findViewById(R.id.etUserId);
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnCreateUser = findViewById(R.id.btnCreateUser);
        btnReadUser = findViewById(R.id.btnReadUser);
        btnUpdateUser = findViewById(R.id.btnUpdateUser);
        btnDeleteUser = findViewById(R.id.btnDeleteUser);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        btnCreateUser.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            User user = new User(nombre, email, password);
            createUser(user);
        });

        btnReadUser.setOnClickListener(v -> {
            int userId;
            try {
                userId = Integer.parseInt(etUserId.getText().toString());
                getUserById(userId);
            } catch (NumberFormatException e) {
                Toast.makeText(UserActivity.this, "ID de usuario inválido.", Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdateUser.setOnClickListener(v -> {
            int userId = Integer.parseInt(etUserId.getText().toString());
            String nombre = etNombre.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            User user = new User(nombre, email, password);
            updateUser(userId, user);
        });

        btnDeleteUser.setOnClickListener(v -> {
            int userId = Integer.parseInt(etUserId.getText().toString());
            deleteUser(userId);
        });
    }
    private void clearFields() {
        etUserId.setText("");
        etNombre.setText("");
        etEmail.setText("");
        etPassword.setText("");
    }
    private void createUser(User user) {
        Call<ApiResponse> call = apiService.createUser(user);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(UserActivity.this, "Usuario creado con éxito", Toast.LENGTH_SHORT).show();
                    clearFields(); // Limpia los campos después de la creación exitosa
                } else {
                    Toast.makeText(UserActivity.this, "Error al crear el usuario", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(UserActivity.this, "Fallo en la comunicación: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserById(int userId) {
        Call<User> call = apiService.getUserById(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    if (user != null) {
                        etNombre.setText(user.getNombre());
                        etEmail.setText(user.getEmail());
                        etPassword.setText(user.getPassword());
                        Toast.makeText(UserActivity.this, "Usuario obtenido con éxito", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserActivity.this, "Usuario no encontrado.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserActivity.this, "Error al leer el usuario", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(UserActivity.this, "Fallo en la comunicación: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUser(int userId, User user) {
        Call<ApiResponse> call = apiService.updateUser(userId, user);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(UserActivity.this, "Usuario actualizado con éxito", Toast.LENGTH_SHORT).show();
                    clearFields(); // Limpia los campos después de la creación exitosa
                } else {
                    Toast.makeText(UserActivity.this, "Error al actualizar el usuario", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(UserActivity.this, "Fallo en la comunicación: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser(int userId) {
        Call<ApiResponse> call = apiService.deleteUser(Collections.singletonMap("id", userId));
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(UserActivity.this, "Usuario eliminado con éxito", Toast.LENGTH_SHORT).show();
                    clearFields(); // Limpia los campos después de la creación exitosa
                } else {
                    Toast.makeText(UserActivity.this, "Error al eliminar el usuario", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(UserActivity.this, "Fallo en la comunicación: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

