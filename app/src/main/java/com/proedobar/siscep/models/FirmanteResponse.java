package com.proedobar.siscep.models;

import com.google.gson.annotations.SerializedName;

public class FirmanteResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private FirmanteData data;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public FirmanteData getData() {
        return data;
    }

    public static class FirmanteData {
        @SerializedName("nombre")
        private String nombre;

        @SerializedName("resolucion")
        private String resolucion;

        @SerializedName("fecha_resolucion")
        private String fechaResolucion;

        @SerializedName("gaceta")
        private String gaceta;

        @SerializedName("fecha_gaceta")
        private String fechaGaceta;

        @SerializedName("activo")
        private boolean activo;

        @SerializedName("isprocurador")
        private boolean isProcurador;

        public String getNombre() {
            return nombre;
        }

        public String getResolucion() {
            return resolucion;
        }

        public String getFechaResolucion() {
            return fechaResolucion;
        }

        public String getGaceta() {
            return gaceta;
        }

        public String getFechaGaceta() {
            return fechaGaceta;
        }

        public boolean isActivo() {
            return activo;
        }

        public boolean isProcurador() {
            return isProcurador;
        }
    }
} 