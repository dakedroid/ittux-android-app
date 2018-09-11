package com.divaga.tecnologico.fragments;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.divaga.tecnologico.R;
import com.divaga.tecnologico.model.Documento;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog Fragment containing Comentario form.
 */
public class PublishDocumentDialogFragment extends DialogFragment{

    public static final String TAG = "PublishDocumentDialog";

    private static final int PICK_IMAGE_REQUEST = 234;




    private Uri filePath;

    private String extension;

    private String name;

    @BindView(R.id.dialog_documento_edtx)
    EditText mDocumentoText;

    @BindView(R.id.spinner)
    Spinner spinner;

    @BindView(R.id.iv_post_display)
    ImageView picture;

    @BindView(R.id.layout_image)
    RelativeLayout rl;

    @BindView(R.id.add_photo_btn)
    ImageButton imageButton;

    @BindView(R.id.progress)
    ProgressBar pbar;



    @BindView(R.id.dialog_documento_filename)
    TextView txtFileName;




    private void showFileChooser() {

        /*
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Escojer una Imagen"), PICK_IMAGE_REQUEST);

*/

        String[] mimeTypes =
                {"application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                        "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                        "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                        "application/pdf",
                        "image/*",
                        "application/zip"};

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
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
                //imagePicked = true;
                //bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                extension = filePath.getPath().substring(filePath.getPath().lastIndexOf("."));

                Log.i("extension", extension);

                name = filePath.getPath().substring(filePath.getPath().lastIndexOf("/"));

                name = name.replace("/", "");

                txtFileName.setText(name);

                int id = 0;

                if (extension.equals(".pdf")){
                    id = R.drawable.pdf;
                }else if (extension.equals(".docx") || extension.equals(".doc") ){
                    id = R.drawable.word;

                }else if (extension.equals(".ppt") || extension.equals(".pptx") ){
                    id = R.drawable.powerpoint;

                }else if (extension.equals(".xls") || extension.equals(".xlsx") ){
                    id = R.drawable.excel;

                }else if (extension.equals(".jpg") || extension.equals(".png")  || extension.equals(".jpeg")){
                    id = R.drawable.picture;

                }else if (extension.equals(".zip")){
                    id = R.drawable.zip;
                }

                Glide.with(this)
                        .load(id)
                        .into(picture);
                // picture.setImageBitmap(bitmap);

               // picture.setScaleType(ImageView.ScaleType.CENTER_CROP);

                picture.setVisibility(View.VISIBLE);
                txtFileName.setVisibility(View.VISIBLE);
                pbar.setVisibility(View.VISIBLE);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    public interface DocumentoListener {

        void onSubirDocumento(Documento documento, Uri filePath);

    }

    private DocumentoListener mDocumentoListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_documento, container, false);
        ButterKnife.bind(this, v);

        String[] s = {getString(R.string.residencias), getString(R.string.servicio_social), getString(R.string.becas), getString(R.string.administrativo)};

        ArrayAdapter<String> adp = new ArrayAdapter<String>(getActivity(), R.layout.spinner_publish, s);

        spinner.setAdapter(adp);

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

        if (context instanceof DocumentoListener) {
            mDocumentoListener = (DocumentoListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }


    @OnClick(R.id.dialog_documento_btn_publicar)
    public void onSubmitClicked(View view) {

        Documento documento = new Documento();

        documento.setName(spinner.getSelectedItem().toString().toLowerCase() + "-" + name);
        documento.setType(extension);
        documento.setCategory(spinner.getSelectedItem().toString());
        documento.setUsername(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        if (mDocumentoListener != null) {

            mDocumentoListener.onSubirDocumento(documento, filePath);
        }

        dismiss();
    }

    @OnClick(R.id.dialog_comentario_cerrar)
    public void onCancelClicked(View view) {
        dismiss();
    }


}
