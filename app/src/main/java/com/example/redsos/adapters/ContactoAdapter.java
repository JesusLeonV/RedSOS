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

/**
 * ContactoAdapter conecta los contactos de la base de datos con el RecyclerView.
 * Por cada contacto crea una tarjeta (item_contacto.xml) y le pone sus datos.
 *
 * Los clics se avisan con la interfaz OnContactoClickListener que
 * implementa la pantalla de contactos.
 */
public class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.ViewHolder> {

    /**
     * Avisa a la pantalla cuando el usuario toca llamar o borrar un contacto.
     */
    public interface OnContactoClickListener {
        /**
         * Se llama cuando tocan el botón de llamar.
         * @param telefono el número del contacto
         */
        void onLlamar(String telefono);

        /**
         * Se llama cuando tocan el botón de borrar.
         * @param id el id del contacto en la base de datos
         * @param posicion su posición en la lista
         */
        void onEliminar(int id, int posicion);
    }

    private final List<String[]> contactos = new ArrayList<>();
    private final OnContactoClickListener listener;

    /**
     * Constructor del adapter.
     * @param listener quién va a recibir los clics (la pantalla de contactos)
     */
    public ContactoAdapter(OnContactoClickListener listener) {
        this.listener = listener;
    }

    /**
     * Reemplaza la lista de contactos y refresca la pantalla.
     * @param nuevos la lista nueva que viene de la base de datos
     */
    public void actualizar(List<String[]> nuevos) {
        contactos.clear();
        contactos.addAll(nuevos);
        notifyDataSetChanged();
    }

    /**
     * Crea una tarjeta nueva inflando el layout del contacto.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contacto, parent, false);
        return new ViewHolder(vista);
    }

    /**
     * Pone los datos del contacto en la tarjeta y arma los botones.
     */
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

    /**
     * @return cuántos contactos hay en la lista
     */
    @Override
    public int getItemCount() {
        return contactos.size();
    }

    /**
     * ViewHolder guarda las vistas de una tarjeta para no buscarlas
     * con findViewById cada vez que se dibuja (eso lo hace más rápido).
     */
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
