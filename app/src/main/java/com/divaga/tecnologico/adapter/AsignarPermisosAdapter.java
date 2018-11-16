package com.divaga.tecnologico.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.divaga.tecnologico.R;
import com.divaga.tecnologico.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * RecyclerView adapter for a list of Publicaciones
 */
public class AsignarPermisosAdapter extends FirestoreAdapter<AsignarPermisosAdapter.ViewHolder> {


    private OnAsignarPermisosSelectedListener mListener;


    public AsignarPermisosAdapter(Query query, OnAsignarPermisosSelectedListener mListener) {
        super(query);
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_asignar_permisos, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    public interface OnAsignarPermisosSelectedListener {
        void OnAsignarPermisosSelected(DocumentSnapshot user);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_asignar_permisos_imagen)
        CircleImageView userPhotoView;

        @BindView(R.id.item_asignar_permisos_username)
        TextView usernameView;

        @BindView(R.id.item_asignar_permisos_numero)
        TextView permissionView;

        @BindView(R.id.item_asignar_permisos_boton)
        LinearLayout asignarPermisoBtn;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot, final OnAsignarPermisosSelectedListener listener) {

            User user = snapshot.toObject(User.class);
            // Resources resources = itemView.getResources();

            // Load image
            Glide.with(userPhotoView.getContext())
                    .load(user.getPhoto())
                    .into(userPhotoView);

            usernameView.setText(user.getEmail());

            String ps = user.getPermisos();

            String psText = "";

            if (ps.contains("1")) {
                psText += "|Avisos|";
            }

            if (ps.contains("2")) {
                psText += "Publicaciones|";
            }

            if (ps.contains("3")) {
                psText += "Convocatorias|";
            }

            if (ps.contains("4")) {
                psText += "Documentos|";
            }

            if (ps.contains("5")) {
                psText += "Administrador|";
            }

            permissionView.setText(psText);


            // Click listener
            asignarPermisoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.OnAsignarPermisosSelected(snapshot);
                    }
                }
            });
        }


    }
}
