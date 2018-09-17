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
import com.divaga.tecnologico.customfonts.MyTextView;
import com.divaga.tecnologico.model.Convocatoria;
import com.divaga.tecnologico.model.Publicacion;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * RecyclerView adapter for a list of Convocatoria
 */
public class ConvocatoriaAdapter extends FirestoreAdapter<ConvocatoriaAdapter.ViewHolder> {


    public interface OnConvocatoriaSelectedListener {
        void OnConvocatoriaSelected(DocumentSnapshot convocatoria);
    }


    private OnConvocatoriaSelectedListener mListener;

    public ConvocatoriaAdapter(Query query, OnConvocatoriaSelectedListener mListener) {
        super(query);
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_convocatoria, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.convocatoria_item_user_photo)
        ImageView user_Photo;

        @BindView(R.id.convocatoria_item_username)
        MyTextView username;

        @BindView(R.id.convocatoria_item_date)
        MyTextView datepublic;

        @BindView(R.id.convocatoria_item_description)
        MyTextView description;

        @BindView(R.id.convocatoria_item_photo)
        ImageView photoView;

        @BindView(R.id.convocatoria_item_date_limit)
        MyTextView dateLimit;

        @BindView(R.id.convocatoria_item_btn_download)
        MyTextView downloadButtonView;


        private static final SimpleDateFormat FORMAT = new SimpleDateFormat(
                "MM/dd/yyyy", Locale.US);


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot, final OnConvocatoriaSelectedListener listener) {

            Convocatoria convocatoria = snapshot.toObject(Convocatoria.class);
            // Resources resources = itemView.getResources();

            int resourceType = 0;

            if(convocatoria.getType().equals(".pdf")){
                resourceType = R.drawable.pdf;
            }else if (convocatoria.getType().equals(".doc") || convocatoria.getType().equals(".docx") ){
                resourceType = R.drawable.word;
            }else if (convocatoria.getType().equals(".ppt") || convocatoria.getType().equals(".pptx") ){
                resourceType = R.drawable.powerpoint;
            }else if (convocatoria.getType().equals(".xls") || convocatoria.getType().equals(".xlsx") ){
                resourceType = R.drawable.excel;
            }else if (convocatoria.getType().equals(".jpg") || convocatoria.getType().equals(".png")  || convocatoria.getType().equals(".jpeg")){
                resourceType = R.drawable.picture;
            }else if (convocatoria.getType().equals(".zip")){
                resourceType = R.drawable.zip;
            }

            // Load image
            Glide.with(user_Photo.getContext())
                    .load(convocatoria.getUser_photo())
                    .into(user_Photo);

            username.setText(convocatoria.getUsername());
            description.setText(convocatoria.getDescription());


            if (convocatoria.getDatepublic() != null) {
                datepublic.setText(FORMAT.format(convocatoria.getDatepublic()));
            }

            Glide.with(photoView.getContext())
                    .load(resourceType)
                    .into(photoView);
            if (convocatoria.getDatelimit() != null) {
                dateLimit.setText(FORMAT.format(convocatoria.getDatelimit()));
            }



                // Click listener
                downloadButtonView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null) {
                            listener.OnConvocatoriaSelected(snapshot);
                        }
                    }
                });
            }

        }
    }

