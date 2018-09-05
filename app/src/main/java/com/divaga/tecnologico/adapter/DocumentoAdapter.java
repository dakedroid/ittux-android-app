package com.divaga.tecnologico.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.divaga.tecnologico.R;
import com.divaga.tecnologico.model.Documento;
import com.divaga.tecnologico.model.Publicacion;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * RecyclerView adapter for a list of Publicaciones
 */
public class DocumentoAdapter extends FirestoreAdapter<DocumentoAdapter.ViewHolder> {


    public interface OnDocumentoSelectedListener {
        void OnDocumentoSelected(DocumentSnapshot documento);
    }


    private OnDocumentoSelectedListener mListener;

    public DocumentoAdapter(Query query, OnDocumentoSelectedListener mListener) {
        super(query);
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_documentos, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_documentos_imagen_documento)
        ImageView typeDocumentView;

        @BindView(R.id.item_documentos_nombre_documento)
        TextView nameDocumentView;

        @BindView(R.id.item_documentos_username)
        TextView usernameView;

        @BindView(R.id.item_documentos_boton_descarga)
        ImageView downloadButtonView;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot, final OnDocumentoSelectedListener listener) {

            Documento documento = snapshot.toObject(Documento.class);
            // Resources resources = itemView.getResources();

            int resourceType = 0;

            if(documento.getType().equals("pdf")){
                resourceType = R.drawable.pdf;

            }else if (documento.getType().equals("docx")){


            }

            // Load image
            Glide.with(typeDocumentView.getContext())
                    .load(resourceType)
                    .into(typeDocumentView);

            nameDocumentView.setText(documento.getName());
            usernameView.setText(documento.getUsername());

            // Click listener
            downloadButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.OnDocumentoSelected(snapshot);
                    }
                }
            });
        }

    }
}
