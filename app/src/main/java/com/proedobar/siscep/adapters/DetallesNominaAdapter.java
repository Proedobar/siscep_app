package com.proedobar.siscep.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.proedobar.siscep.R;
import com.proedobar.siscep.models.DetallesNominaResponse.DetalleNomina;

import java.util.ArrayList;
import java.util.List;

public class DetallesNominaAdapter extends RecyclerView.Adapter<DetallesNominaAdapter.ViewHolder> {
    private List<DetalleNomina> detalles = new ArrayList<>();
    private final OnDetalleClickListener listener;
    private final int mes;
    private final int anio;

    public interface OnDetalleClickListener {
        void onDescargarClick(String detalleId);
    }

    public DetallesNominaAdapter(OnDetalleClickListener listener, int mes, int anio) {
        this.listener = listener;
        this.mes = mes;
        this.anio = anio;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_detalle_nomina, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetalleNomina detalle = detalles.get(position);
        
        // Header
        holder.nombreNominaText.setText(detalle.getNombreNomina());
        holder.periodoText.setText(detalle.getPeriodoNombre());
        
        // Cuerpo
        holder.nombreEmpleadoText.setText("• " + detalle.getNombreEmpleado());
        holder.cestaTicketsText.setText("• Cesta Tickets: " + detalle.getCestaTickets());
        holder.montoPagarText.setText("• Monto a Pagar: " + detalle.getMontoPagar());
        holder.mesAnioText.setText("• Período: " + obtenerNombreMes(mes) + " " + anio);
        
        // Footer
        holder.btnDescargarRecibo.setOnClickListener(v -> 
            listener.onDescargarClick(detalle.getDetailId())
        );
    }

    @Override
    public int getItemCount() {
        return detalles.size();
    }

    public void setDetalles(List<DetalleNomina> detalles) {
        this.detalles = detalles;
        notifyDataSetChanged();
    }

    private String obtenerNombreMes(int mes) {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                         "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[mes - 1];
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nombreNominaText;
        final TextView periodoText;
        final TextView nombreEmpleadoText;
        final TextView cestaTicketsText;
        final TextView montoPagarText;
        final TextView mesAnioText;
        final MaterialButton btnDescargarRecibo;

        ViewHolder(View view) {
            super(view);
            nombreNominaText = view.findViewById(R.id.nombreNominaText);
            periodoText = view.findViewById(R.id.periodoText);
            nombreEmpleadoText = view.findViewById(R.id.nombreEmpleadoText);
            cestaTicketsText = view.findViewById(R.id.cestaTicketsText);
            montoPagarText = view.findViewById(R.id.montoPagarText);
            mesAnioText = view.findViewById(R.id.mesAnioText);
            btnDescargarRecibo = view.findViewById(R.id.btnDescargarRecibo);
        }
    }
} 