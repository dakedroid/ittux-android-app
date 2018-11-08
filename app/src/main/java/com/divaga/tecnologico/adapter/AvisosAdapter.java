package com.divaga.tecnologico.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.divaga.tecnologico.R;
import com.divaga.tecnologico.model.Aviso;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * RecyclerView adapter for a list of Publicaciones
 */
public class AvisosAdapter extends FirestoreAdapter<AvisosAdapter.ViewHolder> {


    public interface OnAvisosSelectedListener {
        void OnAvisosSelected(DocumentSnapshot avisos);
    }


    private OnAvisosSelectedListener mListener;

    public AvisosAdapter(Query query, OnAvisosSelectedListener mListener) {
        super(query);
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_avisos, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.avisos_item_user_photo)
        CircleImageView userPhotoView;

        @BindView(R.id.avisos_item_username)
        TextView usernameView;

        @BindView(R.id.avisos_item_date)
        TextView dateView;

        @BindView(R.id.avisos_item_description)
        TextView descriptionView;



        private static final SimpleDateFormat FORMAT  = new SimpleDateFormat(
                "MM/dd/yyyy", Locale.US);



        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot, final OnAvisosSelectedListener listener) {

            Aviso aviso = snapshot.toObject(Aviso.class);
            // Resources resources = itemView.getResources();

            // Load image
            Glide.with(userPhotoView.getContext())
                    .load(aviso.getUser_photo())
                    .into(userPhotoView);

            usernameView.setText(aviso.getUsername());
            descriptionView.setText(aviso.getDescription());

            if (aviso.getDatepublic() != null) {
                dateView.setText(FORMAT.format(aviso.getDatepublic()));
            }



            // Click listener

        }

    }
}
