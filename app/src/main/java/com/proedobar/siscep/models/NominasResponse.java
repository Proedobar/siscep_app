package com.proedobar.siscep.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NominasResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private List<NominaData> data;

    public String getStatus() {
        return status;
    }

    public List<NominaData> getData() {
        return data;
    }

    public static class NominaData {
        @SerializedName("id")
        private String id;

        @SerializedName("nombre")
        private String nombre;

        public String getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }
    }
} 