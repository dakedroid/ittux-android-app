package com.divaga.tecnologico.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.divaga.tecnologico.R;
import com.divaga.tecnologico.Utils.GlideApp;
import com.divaga.tecnologico.model.Publicacion;
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
public class PublicacionAdapter extends FirestoreAdapter<PublicacionAdapter.ViewHolder> {

    private Context mContext;


    public interface OnPublicacionSelectedListener {
        void OnPublicacionSelected(DocumentSnapshot publicacion);
        void OnShareSelected(String title, LinearLayout photo);
    }


    private OnPublicacionSelectedListener mListener;

    public PublicacionAdapter(Query query, OnPublicacionSelectedListener mListener, Context context) {
        super(query);
        this.mListener = mListener;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_publicacion, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener, mContext);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.publicacion_item_user_photo)
        CircleImageView userPhotoView;

        @BindView(R.id.publicacion_item_username)
        TextView usernameView;

        @BindView(R.id.publicacion_item_date)
        TextView dateView;

        @BindView(R.id.publicacion_item_description)
        TextView descriptionView;

        @BindView(R.id.publicacion_item_photo)
        ImageView photoView;


        @BindView(R.id.publicacion_item_num_comments)
        TextView numCommentsView;

        @BindView(R.id.postPhotoButton)
        Button postPhotoButton;


        @BindView(R.id.item_publicacion_share_layout)
        LinearLayout ll;


        private static final SimpleDateFormat FORMAT  = new SimpleDateFormat(
                "MM/dd/yyyy", Locale.US);


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setImage(){


        }

        public void bind(final DocumentSnapshot snapshot, final OnPublicacionSelectedListener listener, final Context context) {


            final Publicacion publicacion = snapshot.toObject(Publicacion.class);
            // Resources resources = itemView.getResources();

            // Load image
            Glide.with(userPhotoView.getContext())
                    .load(publicacion.getUser_photo())
                    .into(userPhotoView);
            
            if (publicacion.getTimestamp() != null) {

                dateView.setText(FORMAT.format(publicacion.getTimestamp()));
            }

            usernameView.setText(publicacion.getUsername());
            descriptionView.setText(publicacion.getDescription());

            if (publicacion.getTimestamp() != null) {
                dateView.setText(FORMAT.format(publicacion.getTimestamp()));
            }


            GlideApp.with(context)
                    .load(publicacion.getPhoto())
                    .into(photoView);


            postPhotoButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (listener != null) {
                        listener.OnShareSelected(descriptionView.getText().toString(),ll);
                    }
                }
            });

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
