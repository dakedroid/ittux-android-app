package com.divaga.tecnologico.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.divaga.tecnologico.R;
import com.divaga.tecnologico.model.Convocatoria;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog Fragment containing Comentario form.
 */
public class PublishConvocatoriaDialogFragment extends DialogFragment{

    public static final String TAG = "PublishConvocatoriaDialog";

    private static final int PICK_IMAGE_REQUEST = 234;

    private Uri filePath;

    private String extension;

    private String name;


    private String finalDate;


    // date variables
    private static final String CERO = "0";
    private static final String DOS_PUNTOS = ":";
    private static final String BARRA = "/";

    //Calendario para obtener fecha & hora
    public final Calendar c = Calendar.getInstance();

    //Fecha
    final int mes = c.get(Calendar.MONTH);
    final int dia = c.get(Calendar.DAY_OF_MONTH);
    final int anio = c.get(Calendar.YEAR);

    //Hora
    final int hora = c.get(Calendar.HOUR_OF_DAY);
    final int minuto = c.get(Calendar.MINUTE);


    @BindView(R.id.dialog_convocatoria_edtx)
    EditText mDocumentoText;

    @BindView(R.id.iv_post_display)
    ImageView picture;

    @BindView(R.id.layout_image)
    RelativeLayout rl;

    @BindView(R.id.add_photo_btn)
    ImageButton imageButton;

    @BindView(R.id.progress)
    ProgressBar pbar;


    //Widgets
    @BindView(R.id.dialog_convocatoria_edtx_fecha)
    EditText etFecha;

    @BindView(R.id.dialog_convocatoria_btn_fecha)
    ImageButton ibObtenerFecha;


    private void showFileChooser() {

        String[] mimeTypes = {"application/pdf", "image/*"};

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }

        startActivityForResult(Intent.createChooser(intent,"Escoger un archivo"), PICK_IMAGE_REQUEST);




    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {


                extension = filePath.getPath().substring(filePath.getPath().lastIndexOf("."));

                Log.i("extension", extension);

                name = filePath.getPath().substring(filePath.getPath().lastIndexOf("/"));

                name = name.replace("/", "");

              //  Toast.makeText(getContext(), "Archivo seleccionado: " +  name, Toast.LENGTH_SHORT).show();


                //Toast.makeText(getContext(), extension, Toast.LENGTH_SHORT).show();


                if (extension.equals(".pdf")){
                    int id = R.drawable.pdf;

                    Glide.with(this)
                            .load(id)
                            .into(picture);

                }else if (extension.equals(".jpg") || extension.equals(".png")  || extension.equals(".jpeg")){

                    Glide.with(this)
                            .load(filePath)
                            .into(picture);

                }

                picture.setVisibility(View.VISIBLE);
                pbar.setVisibility(View.VISIBLE);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    public interface ConvocatoriaListener {

        void onSubirConvocatoria(Convocatoria convocatoria, Uri filePath);

    }

    private ConvocatoriaListener mConvocatoriaListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_convocatoria, container, false);
        ButterKnife.bind(this, v);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        ibObtenerFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                obtenerFecha();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ConvocatoriaListener) {
            mConvocatoriaListener = (ConvocatoriaListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("dd-MM-yyyy").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    private void obtenerFecha(){
        DatePickerDialog recogerFecha = new DatePickerDialog(Objects.requireNonNull(getContext()), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {


                final int mesActual = month + 1;

                String diaFormateado = (dayOfMonth < 10)? CERO + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
                String mesFormateado = (mesActual < 10)? CERO + String.valueOf(mesActual):String.valueOf(mesActual);


                finalDate = diaFormateado + BARRA + mesFormateado + BARRA + year;

                etFecha.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);


            }
        },anio, mes, dia);


        recogerFecha.show();

    }

    @OnClick(R.id.dialog_convocatoria_btn_publicar)
    public void onSubmitClicked(View view) {



        Convocatoria convocatoria = new Convocatoria();

        convocatoria.setType(extension);
        convocatoria.setDescription(mDocumentoText.getText().toString());
        convocatoria.setDatelimit(finalDate);

        if (mConvocatoriaListener != null) {

            mConvocatoriaListener.onSubirConvocatoria(convocatoria, filePath);
        }

        dismiss();
    }

    @OnClick(R.id.dialog_convocatoria_cerrar)
    public void onCancelClicked(View view) {
        dismiss();
    }


}
