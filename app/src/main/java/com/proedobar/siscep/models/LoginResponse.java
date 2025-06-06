package com.proedobar.siscep.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class LoginResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private UserData data;

    public String getStatus() {
        return status;
    }

    public UserData getData() {
        return data;
    }

    public static class UserData {
        @SerializedName("user_id")
        private int userId;

        @SerializedName("nombre")
        private String nombre;

        @SerializedName("cedula")
        private String cedula;

        @SerializedName("fecha_ingreso")
        private String fechaIngreso;

        @SerializedName("cargo")
        private String cargo;

        @SerializedName("tipo_cargo")
        private String tipoCargo;

        @SerializedName("nominas")
        private Map<String, NominaInfo> nominas;

        public int getUserId() {
            return userId;
        }

        public String getNombre() {
            return nombre;
        }

        public String getCedula() {
            return cedula;
        }

        public String getFechaIngreso() {
            return fechaIngreso;
        }

        public String getCargo() {
            return cargo;
        }

        public String getTipoCargo() {
            return tipoCargo;
        }

        public Map<String, NominaInfo> getNominas() {
            return nominas;
        }
    }

    public static class NominaInfo {
        @SerializedName("id")
        private int id;

        @SerializedName("nombre")
        private String nombre;

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }
    }
} 