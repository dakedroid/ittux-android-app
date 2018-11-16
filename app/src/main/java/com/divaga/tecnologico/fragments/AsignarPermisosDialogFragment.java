package com.divaga.tecnologico.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.divaga.tecnologico.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog Fragment containing Comentario form.
 */
public class AsignarPermisosDialogFragment extends DialogFragment {
    public static final String TAG = "AsignarPermisosDialog";

    int[] permisos;

    @BindView(R.id.asignar_permisos_btn_1)
    Button mButton1;

    @BindView(R.id.asignar_permisos_btn_2)
    Button mButton2;

    @BindView(R.id.asignar_permisos_btn_3)
    Button mButton3;

    @BindView(R.id.asignar_permisos_btn_4)
    Button mButton4;

    @BindView(R.id.asignar_permisos_btn_5)
    Button mButton5;

    @BindView(R.id.asignar_permisos_btn_6)
    Button mButton6;


    private AsignarPermisosListener mAsignarPermisosListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_asignar_permisos, container, false);
        ButterKnife.bind(this, v);


        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btToggleClick(mButton1);
                btToggleClick(mButton2);
                btToggleClick(mButton3);
                btToggleClick(mButton4);
                btToggleClick(mButton5);
                btToggleClick(mButton6);

            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btToggleClick(mButton2);
            }
        });

        mButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btToggleClick(mButton3);
            }
        });

        mButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btToggleClick(mButton4);
            }
        });

        mButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btToggleClick(mButton5);
            }
        });

        mButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btToggleClick(mButton6);
            }
        });

        return v;
    }

    public void btToggleClick(View view) {
        if (view instanceof Button) {
            Button b = (Button) view;
            if (b.isSelected()) {
                b.setTextColor(getResources().getColor(R.color.grey_40));
            } else {
                b.setTextColor(Color.WHITE);
            }
            b.setSelected(!b.isSelected());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof AsignarPermisosListener) {
            mAsignarPermisosListener = (AsignarPermisosListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }


    @OnClick(R.id.dialog_asingar_permisos_btn)
    public void onSubmitClicked(View view) {

        if (mAsignarPermisosListener != null) {


            permisos = new int[5];


            if (mButton1.getCurrentTextColor() == Color.WHITE) {
                permisos[0] = 1;
                permisos[1] = 1;
                permisos[2] = 1;
                permisos[3] = 1;
                permisos[4] = 1;
            }

            if (mButton2.getCurrentTextColor() == Color.WHITE) {
                permisos[0] = 1;
            }
            if (mButton3.getCurrentTextColor() == Color.WHITE) {
                permisos[1] = 1;
            }
            if (mButton4.getCurrentTextColor() == Color.WHITE) {
                permisos[2] = 1;
            }
            if (mButton5.getCurrentTextColor() == Color.WHITE) {
                permisos[3] = 1;
            }
            if (mButton6.getCurrentTextColor() == Color.WHITE) {
                permisos[4] = 1;
            }


            mAsignarPermisosListener.onSubirPermiso(permisos);
        }

        dismiss();
    }

    @OnClick(R.id.dialog_asignar_permisos_cerrar)
    public void onCancelClicked(View view) {
        dismiss();
    }


    public interface AsignarPermisosListener {

        void onSubirPermiso(int[] permisos);

    }


}
