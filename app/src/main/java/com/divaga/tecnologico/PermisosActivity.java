package com.divaga.tecnologico;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class PermisosActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permisos);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id == R.id.permisos_btn_1){


        }else if (id == R.id.permisos_btn_2){


        }else if (id == R.id.permisos_btn_3){


        }else if (id == R.id.permisos_btn_4){


        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
