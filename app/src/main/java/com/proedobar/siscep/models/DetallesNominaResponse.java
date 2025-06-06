package com.proedobar.siscep.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DetallesNominaResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<DetalleNomina> data;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<DetalleNomina> getData() {
        return data;
    }

    public static class DetalleNomina {
        @SerializedName("detail_id")
        private String detailId;

        @SerializedName("nombre_nomina")
        private String nombreNomina;

        @SerializedName("periodo_nombre")
        private String periodoNombre;

        @SerializedName("nombre_empleado")
        private String nombreEmpleado;

        @SerializedName("montopagar")
        private String montoPagar;

        @SerializedName("cesta_tickets")
        private String cestaTickets;

        public String getDetailId() {
            return detailId;
        }

        public String getNombreNomina() {
            return nombreNomina;
        }

        public String getPeriodoNombre() {
            return periodoNombre;
        }

        public String getNombreEmpleado() {
            return nombreEmpleado;
        }

        public String getMontoPagar() {
            return montoPagar;
        }

        public String getCestaTickets() {
            return cestaTickets;
        }
    }
} 