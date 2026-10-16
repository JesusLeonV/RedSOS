package com.example.redsos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.redsos.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.ViewHolder> {

    public interface OnContactoClickListener {
        void onLlamar(String telefono);
        void onEliminar(int id, int posicion);
    }

    private final List<String[]> contactos = new ArrayList<>();
    private final OnContactoClickListener listener;

    public ContactoAdapter(OnContactoClickListener listener) {
        this.listener = listener;
    }

    public void actualizar(List<String[]> nuevos) {
        contactos.clear();
        contactos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contacto, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] fila = contactos.get(position);
        holder.txtNombre.setText(fila[1]);
        String parentesco = fila[2] != null && !fila[2].isEmpty() ? fila[2] : "Contacto";
        holder.txtDetalle.setText(parentesco + " · " + fila[3]);

        holder.btnLlamar.setOnClickListener(v ->
                listener.onLlamar(fila[3]));

        holder.btnEliminar.setOnClickListener(v ->
                listener.onEliminar(Integer.parseInt(fila[0]), holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return contactos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtDetalle;
        MaterialButton btnLlamar;
        ImageButton btnEliminar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreContacto);
            txtDetalle = itemView.findViewById(R.id.txtDetalleContacto);
            btnLlamar = itemView.findViewById(R.id.btnLlamarContacto);
            btnEliminar = itemView.findViewById(R.id.btnEliminarContacto);
        }
    }
}
