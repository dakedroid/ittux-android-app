package com.divaga.tecnologico;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


import com.divaga.tecnologico.model.Comentario;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog Fragment containing Comentario form.
 */
public class ComentarioDialogFragment extends DialogFragment {

    public static final String TAG = "ComentarioDialog";

    @BindView(R.id.publicacion_form_text)
    EditText mComentarioText;

    interface ComentarioListener {

        void onComentar(Comentario comentario);

    }

    private ComentarioListener mComentarioListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_comentario, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ComentarioListener) {
            mComentarioListener = (ComentarioListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    @OnClick(R.id.publicacion_form_button)
    public void onSubmitClicked(View view) {
        Comentario comentario = new Comentario(
                FirebaseAuth.getInstance().getCurrentUser(),
                mComentarioText.getText().toString());

        if (mComentarioListener != null) {
            mComentarioListener.onComentar(comentario);
        }

        dismiss();
    }

    @OnClick(R.id.publicacion_form_cancel)
    public void onCancelClicked(View view) {
        dismiss();
    }
}
