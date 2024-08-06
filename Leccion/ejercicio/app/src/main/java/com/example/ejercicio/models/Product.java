package com.example.ejercicio.models;

public class Product {
    private int id;
    private String nombre;
    private String descripcion;
    private String precio;
    private int id_usuario;

    // Constructor vacío
    public Product() {}

    // Constructor con parámetros
    public Product(String nombre, String descripcion, String precio, int id_usuario) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.id_usuario = id_usuario;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    // Getter y setter para 'nombre'
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Getter y setter para 'descripcion'
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Getter y setter para 'precio'
    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    // Getter y setter para 'id_usuario'
    public int getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }
}
