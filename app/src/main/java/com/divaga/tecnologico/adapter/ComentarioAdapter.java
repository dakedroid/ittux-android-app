package com.divaga.tecnologico.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.divaga.tecnologico.R;

import com.divaga.tecnologico.model.Comentario;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ComentarioAdapter extends FirestoreAdapter<ComentarioAdapter.ViewHolder> {

    public ComentarioAdapter(Query query) {
        super(query);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position).toObject(Comentario.class));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private static final SimpleDateFormat FORMAT  = new SimpleDateFormat(
                "MM/dd/yyyy", Locale.US);

        @BindView(R.id.comentario_item_username)
        TextView nameView;


        @BindView(R.id.comentario_item_text)
        TextView textView;

        @BindView(R.id.comentario_item_date)
        TextView dateView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Comentario comentario) {
            nameView.setText(comentario.getUserName());

            textView.setText(comentario.getText());

            if (comentario.getTimestamp() != null) {
                dateView.setText(FORMAT.format(comentario.getTimestamp()));
            }
        }
    }
}
