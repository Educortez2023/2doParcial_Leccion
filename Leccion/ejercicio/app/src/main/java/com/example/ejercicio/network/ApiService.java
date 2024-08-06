package com.example.ejercicio.network;

import com.example.ejercicio.models.User;
import com.example.ejercicio.models.Product;
import com.example.ejercicio.models.ApiResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("/api/usuarios/create")
    Call<ApiResponse> createUser(@Body User user);

    @GET("/api/usuarios/read/id={id}")
    Call<User> getUserById(@Path("id") int id);

    @POST("/api/usuarios/update/id={id}")
    Call<ApiResponse> updateUser(@Path("id") int id, @Body User user);

    @POST("/api/usuarios/delete")
    Call<ApiResponse> deleteUser(@Body Map<String, Integer> id);

    @POST("/api/productos/create")
    Call<ApiResponse> createProduct(@Body Product product);

    @GET("/api/productos/list/id_usuario={id_usuario}")
    Call<List<Product>> getProductsByUserId(@Path("id_usuario") int idUsuario);

    @GET("/api/productos/read/id={id}")
    Call<Product> getProductById(@Path("id") int id);

    @POST("/api/productos/update/id={id}")
    Call<ApiResponse> updateProduct(@Path("id") int id, @Body Product product);

    @POST("/api/productos/delete")
    Call<ApiResponse> deleteProduct(@Body Map<String, Integer> id);

}
