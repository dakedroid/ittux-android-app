package com.divaga.tecnologico;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.divaga.tecnologico.permissions.AsignarPermisosActivity;

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
            startActivity(new Intent(PermisosActivity.this, AsignarPermisosActivity.class));

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
