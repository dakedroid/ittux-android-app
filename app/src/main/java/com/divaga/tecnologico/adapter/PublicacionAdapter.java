package com.divaga.tecnologico.adapter;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.divaga.tecnologico.R;
import com.divaga.tecnologico.model.Publicacion;
import com.divaga.tecnologico.model.Restaurant;
import com.divaga.tecnologico.util.RestaurantUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * RecyclerView adapter for a list of Publicaciones
 */
public class PublicacionAdapter extends FirestoreAdapter<PublicacionAdapter.ViewHolder> {


    public interface OnPublicacionSelectedListener {
        void OnPublicacionSelected(DocumentSnapshot publicacion);
    }


    private OnPublicacionSelectedListener mListener;

    public PublicacionAdapter(Query query, OnPublicacionSelectedListener mListener) {
        super(query);
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_publicacion, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.publicacion_item_user_photo)
        ImageView userPhotoView;

        @BindView(R.id.publicacion_item_username)
        TextView usernameView;

        @BindView(R.id.publicacion_item_date)
        TextView dateView;

        @BindView(R.id.publicacion_item_description)
        TextView descriptionView;

        @BindView(R.id.publicacion_item_photo)
        ImageView photoView;

        @BindView(R.id.publicacion_item_num_likes)
        TextView numLikesView;

        @BindView(R.id.publicacion_item_num_comments)
        TextView numCommentsView;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot, final OnPublicacionSelectedListener listener) {

            Publicacion publicacion = snapshot.toObject(Publicacion.class);
            // Resources resources = itemView.getResources();

            // Load image
            Glide.with(userPhotoView.getContext())
                    .load(publicacion.getUser_photo())
                    .into(userPhotoView);

            usernameView.setText(publicacion.getUsername());
            descriptionView.setText(publicacion.getDescription());

            Glide.with(photoView.getContext())
                    .load(publicacion.getPhoto())
                    .into(photoView);

            numLikesView.setText(String.valueOf(publicacion.getNumLikes()));
            numCommentsView.setText(String.valueOf(publicacion.getNumComments()));


            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.OnPublicacionSelected(snapshot);
                    }
                }
            });
        }

    }
}
