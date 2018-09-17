package com.divaga.tecnologico.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.divaga.tecnologico.R;
import com.divaga.tecnologico.model.Documento;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * RecyclerView adapter for a list of Publicaciones
 */
public class DocumentoAdapter extends FirestoreAdapter<DocumentoAdapter.ViewHolder> {

    public Context context;

    public interface OnDocumentoSelectedListener {
        void OnDocumentoSelected(DocumentSnapshot documento);
    }


    private OnDocumentoSelectedListener mListener;

    public DocumentoAdapter(Query query, OnDocumentoSelectedListener mListener, Context context) {
        super(query);
        this.mListener = mListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_documentos, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener, context);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_documentos_imagen_documento)
        ImageView typeDocumentView;

        @BindView(R.id.item_documentos_nombre_documento)
        TextView nameDocumentView;

        @BindView(R.id.item_documentos_username)
        TextView usernameView;

        @BindView(R.id.item_documentos_category)
        CardView categoryView;

        @BindView(R.id.item_documentos_txt_category)
        TextView txtCategoryView;


        @BindView(R.id.item_documentos_boton_descarga)
        ImageView downloadButtonView;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot, final OnDocumentoSelectedListener listener, final Context context) {

            Documento documento = snapshot.toObject(Documento.class);
            // Resources resources = itemView.getResources();

            int resourceType = 0;

            assert documento != null;


            if(documento.getType() != null){

                switch (documento.getType()) {
                    case ".pdf":
                        resourceType = R.drawable.pdf;
                        break;
                    case ".doc":
                    case ".docx":
                        resourceType = R.drawable.word;
                        break;
                    case ".ppt":
                    case ".pptx":
                        resourceType = R.drawable.powerpoint;
                        break;
                    case ".xls":
                    case ".xlsx":
                        resourceType = R.drawable.excel;
                        break;
                    case ".jpg":
                    case ".png":
                    case ".jpeg":
                        resourceType = R.drawable.picture;
                        break;
                    case ".zip":
                        resourceType = R.drawable.zip;
                        break;
                }
            }


            Resources resources = context.getResources();

            if(documento.getCategory().equals(resources.getString(R.string.residencias))){

                categoryView.setCardBackgroundColor(resources.getColor(R.color.colorDocument1));
                txtCategoryView.setText(R.string.residencias);

            }else if (documento.getCategory().equals(resources.getString(R.string.servicio_social))){

                categoryView.setCardBackgroundColor(resources.getColor(R.color.colorDocument2));
                txtCategoryView.setText(R.string.servicio_social);

            }else if (documento.getCategory().equals(resources.getString(R.string.becas))){

                categoryView.setCardBackgroundColor(resources.getColor(R.color.colorDocument3));
                txtCategoryView.setText(R.string.becas);

            }else if (documento.getCategory().equals(resources.getString(R.string.administrativo))){

                categoryView.setCardBackgroundColor(resources.getColor(R.color.colorDocument4));
                txtCategoryView.setText(R.string.administrativo);

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
