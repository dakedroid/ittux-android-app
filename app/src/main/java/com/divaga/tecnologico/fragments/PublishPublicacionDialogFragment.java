package com.divaga.tecnologico.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.divaga.tecnologico.R;
import com.divaga.tecnologico.model.Publicacion;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog Fragment containing Comentario form.
 */
public class PublishPublicacionDialogFragment extends DialogFragment{

    public static final String TAG = "PublishDocumentDialog";

    private static final int PICK_IMAGE_REQUEST = 234;

    private Uri filePath;

    @BindView(R.id.dialog_publicacion_edtx)
    EditText mDocumentoText;

    @BindView(R.id.iv_post_display)
    ImageView picture;

    @BindView(R.id.layout_image)
    RelativeLayout rl;

    @BindView(R.id.add_photo_btn)
    ImageButton imageButton;

    @BindView(R.id.progress)
    ProgressBar pbar;


    private void showFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Escojer una Imagen"), PICK_IMAGE_REQUEST);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {


                Glide.with(this)
                        .load(filePath)
                        .into(picture);

                picture.setVisibility(View.VISIBLE);
                pbar.setVisibility(View.VISIBLE);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface PublicacionListener {

        void onSubirPublicacion(Publicacion documento, Uri filePath);

    }

    private PublicacionListener mPublicacionListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_inicio, container, false);
        ButterKnife.bind(this, v);

        String[] s = {getString(R.string.residencias), getString(R.string.servicio_social), getString(R.string.becas), getString(R.string.administrativo)};

        ArrayAdapter<String> adp = new ArrayAdapter<String>(getActivity(), R.layout.spinner_publish, s);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof PublicacionListener) {
            mPublicacionListener = (PublicacionListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }


    @OnClick(R.id.dialog_publicacion_btn_publicar)
    public void onSubmitClicked(View view) {

        Publicacion publicacion = new Publicacion();

        publicacion.setDescription("");
        publicacion.setNumComments(0);
        publicacion.setDescription(mDocumentoText.getText().toString());



        if (mPublicacionListener != null) {

            mPublicacionListener.onSubirPublicacion(publicacion, filePath);
        }

        dismiss();
    }

    @OnClick(R.id.dialog_publicacion_cerrar)
    public void onCancelClicked(View view) {
        dismiss();
    }


}
