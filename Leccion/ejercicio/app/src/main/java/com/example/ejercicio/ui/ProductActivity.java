package com.example.ejercicio.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ejercicio.R;
import com.example.ejercicio.adapters.ProductAdapter;
import com.example.ejercicio.models.ApiResponse;
import com.example.ejercicio.models.Product;
import com.example.ejercicio.models.User;
import com.example.ejercicio.network.ApiService;
import com.example.ejercicio.network.RetrofitClient;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductActivity extends AppCompatActivity {

    private ApiService apiService;
    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private EditText etProductId, etProductName, etProductDescription, etProductPrice, etUserId;
    private Button btnCreateProduct, btnUpdateProduct, btnDeleteProduct, btnListProducts, btnReadProduct ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        etProductId = findViewById(R.id.etProductId);
        etProductName = findViewById(R.id.etProductName);
        etProductDescription = findViewById(R.id.etProductDescription);
        etProductPrice = findViewById(R.id.etProductPrice);
        etUserId = findViewById(R.id.etUserId);
        btnCreateProduct = findViewById(R.id.btnCreateProduct);
        btnReadProduct = findViewById(R.id.btnReadProduct);
        btnUpdateProduct = findViewById(R.id.btnUpdateProduct);
        btnDeleteProduct = findViewById(R.id.btnDeleteProduct);
        btnListProducts = findViewById(R.id.btnListProducts);
        rvProducts = findViewById(R.id.rvProducts);

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        apiService = RetrofitClient.getClient().create(ApiService.class);

        btnCreateProduct.setOnClickListener(v -> {
            String nombre = etProductName.getText().toString();
            String descripcion = etProductDescription.getText().toString();
            String precio = etProductPrice.getText().toString();
            int idUsuario = Integer.parseInt(etUserId.getText().toString());
            Product product = new Product(nombre, descripcion, precio, idUsuario);
            createProduct(product);
        });

        btnReadProduct.setOnClickListener(v -> {
            int ProductId;
            try {
                ProductId = Integer.parseInt(etProductId.getText().toString());
                getProductById(ProductId);
            } catch (NumberFormatException e) {
                Toast.makeText(ProductActivity.this, "ID de producto inválido.", Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdateProduct.setOnClickListener(v -> {
            int productId;
            try {
                productId = Integer.parseInt(etProductId.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(ProductActivity.this, "ID del producto inválido.", Toast.LENGTH_SHORT).show();
                return;
            }

            Product product = new Product();

            String nombre = etProductName.getText().toString();
            String descripcion = etProductDescription.getText().toString();
            String precio = etProductPrice.getText().toString();
            String idUsuario = etUserId.getText().toString();

            if (!nombre.isEmpty()) product.setNombre(nombre);
            if (!descripcion.isEmpty()) product.setDescripcion(descripcion);
            if (!precio.isEmpty()) {
                try {
                    product.setPrecio(precio);
                } catch (NumberFormatException e) {
                    Toast.makeText(ProductActivity.this, "Precio inválido.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (!idUsuario.isEmpty()) {
                try {
                    product.setId_usuario(Integer.parseInt(idUsuario));
                } catch (NumberFormatException e) {
                    Toast.makeText(ProductActivity.this, "ID de usuario inválido.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            updateProduct(productId, product);
        });

        btnDeleteProduct.setOnClickListener(v -> {
            int productId = Integer.parseInt(etProductId.getText().toString());
            deleteProduct(productId);
        });

        btnListProducts.setOnClickListener(v -> {
            int userId = Integer.parseInt(etUserId.getText().toString());
            listProducts(userId);
        });
    }

    private void clearFields() {
        etProductId.setText("");
        etProductName.setText("");
        etProductDescription.setText("");
        etProductPrice.setText("");
        etUserId.setText("");
    }

    private void createProduct(Product product) {
        Call<ApiResponse> call = apiService.createProduct(product);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ProductActivity.this, "Producto creado con éxito", Toast.LENGTH_SHORT).show();
                    clearFields(); // Limpia los campos después de la creación exitosa
                } else {
                    Toast.makeText(ProductActivity.this, "Error al crear el producto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(ProductActivity.this, "Fallo en la comunicación: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateProduct(int productId, Product product) {
        Call<ApiResponse> call = apiService.updateProduct(productId, product);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProductActivity.this, "Producto actualizado con éxito", Toast.LENGTH_SHORT).show();
                    clearFields(); // Limpia los campos después de la creación exitosa
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(ProductActivity.this, "Error al actualizar el producto: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(ProductActivity.this, "Error desconocido al actualizar el producto", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(ProductActivity.this, "Fallo en la comunicación: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProduct(int productId) {
        Call<ApiResponse> call = apiService.deleteProduct(Collections.singletonMap("id", productId));
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ProductActivity.this, "Producto eliminado con éxito", Toast.LENGTH_SHORT).show();
                    clearFields(); // Limpia los campos después de la creación exitosa
                } else {
                    Toast.makeText(ProductActivity.this, "Error al eliminar el producto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(ProductActivity.this, "Fallo en la comunicación: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listProducts(int userId) {
        Call<List<Product>> call = apiService.getProductsByUserId(userId);
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    productAdapter = new ProductAdapter(products);
                    rvProducts.setAdapter(productAdapter);
                    Toast.makeText(ProductActivity.this, "Productos listados con éxito", Toast.LENGTH_SHORT).show();
                    clearFields(); // Limpia los campos después de la creación exitosa
                } else {
                    Toast.makeText(ProductActivity.this, "Error al listar productos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(ProductActivity.this, "Fallo en la comunicación: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getProductById(int ProductId) {
        Call<Product> call = apiService.getProductById(ProductId);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body();
                    if (product != null) {
                        etProductName.setText(product.getNombre());
                        etProductDescription.setText(product.getDescripcion());
                        etProductPrice.setText(product.getPrecio());
                        etUserId.setText(String.valueOf(product.getId_usuario()));
                        Toast.makeText(ProductActivity.this, "Producto obtenido con éxito", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductActivity.this, "Producto no encontrado.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProductActivity.this, "Error al leer el producto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Toast.makeText(ProductActivity.this, "Fallo en la comunicación: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

