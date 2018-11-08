package com.divaga.tecnologico.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.divaga.tecnologico.R;
import com.divaga.tecnologico.model.Aviso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog Fragment containing Comentario form.
 */
public class PublishAvisoDialogFragment extends DialogFragment {

    public static final String TAG = "PublishAvisoDialog";

    @BindView(R.id.dialog_aviso_edtx)
    EditText mDocumentoText;
    private AvisoListener mAvisoListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_aviso, container, false);
        ButterKnife.bind(this, v);


        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof AvisoListener) {
            mAvisoListener = (AvisoListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    @OnClick(R.id.dialog_aviso_btn_publicar)
    public void onSubmitClicked(View view) {

        Aviso aviso = new Aviso();

        aviso.setDescription(mDocumentoText.getText().toString());


        if (mAvisoListener != null) {

            mAvisoListener.onSubirAviso(aviso);
        }

        dismiss();
    }

    @OnClick(R.id.dialog_aviso_cerrar)
    public void onCancelClicked(View view) {
        dismiss();
    }

    public interface AvisoListener {

        void onSubirAviso(Aviso aviso);

    }


}
