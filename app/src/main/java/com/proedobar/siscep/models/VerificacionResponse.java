package com.proedobar.siscep.models;

import com.google.gson.annotations.SerializedName;

public class VerificacionResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private ConstanciaData data;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public ConstanciaData getData() {
        return data;
    }

    public static class ConstanciaData {
        @SerializedName("constancia_id")
        private int constanciaId;

        @SerializedName("empleado")
        private EmpleadoData empleado;

        @SerializedName("fecha_creacion")
        private String fechaCreacion;

        public int getConstanciaId() {
            return constanciaId;
        }

        public EmpleadoData getEmpleado() {
            return empleado;
        }

        public String getFechaCreacion() {
            return fechaCreacion;
        }
    }

    public static class EmpleadoData {
        @SerializedName("nombre")
        private String nombre;

        @SerializedName("user_id")
        private int userId;

        public String getNombre() {
            return nombre;
        }

        public int getUserId() {
            return userId;
        }
    }
} 