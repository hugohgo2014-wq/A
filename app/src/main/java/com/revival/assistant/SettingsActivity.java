package com.revival.assistant;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.widget.*;
import android.view.View;
import java.io.*;

public class SettingsActivity extends Activity {
    private static final int PICK_MODEL = 41;
    private TextView modelStatus;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,24,24,24); root.setBackgroundColor(0xff10131a);
        TextView title = new TextView(this); title.setText("Configurações"); title.setTextSize(26); title.setTextColor(0xffffffff); root.addView(title);
        TextView label = new TextView(this); label.setText("Renderização da interface"); label.setTextColor(0xffdddddd); label.setPadding(0,28,0,8); root.addView(label);
        RadioGroup group = new RadioGroup(this); group.setOrientation(RadioGroup.VERTICAL);
        RadioButton java = new RadioButton(this); java.setText("Java (Android nativo)"); java.setTextColor(0xffffffff); java.setId(View.generateViewId());
        RadioButton gl = new RadioButton(this); gl.setText("OpenGL ES 3.0 (C++)"); gl.setTextColor(0xffffffff); gl.setId(View.generateViewId()); group.addView(java); group.addView(gl); root.addView(group);
        boolean useGl = getSharedPreferences("assistant_settings", MODE_PRIVATE).getBoolean("use_gl", false); group.check(useGl ? gl.getId() : java.getId());
        group.setOnCheckedChangeListener((g,id)->getSharedPreferences("assistant_settings", MODE_PRIVATE).edit().putBoolean("use_gl", id==gl.getId()).apply());
        TextView modelTitle = new TextView(this); modelTitle.setText("Modelo local"); modelTitle.setTextColor(0xffdddddd); modelTitle.setPadding(0,28,0,8); root.addView(modelTitle);
        modelStatus = new TextView(this); modelStatus.setText(getFilesDir()+"/gemma-3-1b-it-Q4_K_M.gguf"); modelStatus.setTextColor(0xffaaaaaa); root.addView(modelStatus);
        Button importButton = new Button(this); importButton.setText("Importar modelo GGUF"); importButton.setOnClickListener(v->{ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.setType("*/*"); i.addCategory(Intent.CATEGORY_OPENABLE); startActivityForResult(i,PICK_MODEL); }); root.addView(importButton);
        Button back = new Button(this); back.setText("Voltar"); back.setOnClickListener(v->finish()); root.addView(back);
        setContentView(root);
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==PICK_MODEL && resultCode==RESULT_OK && data!=null){
            Uri uri=data.getData(); modelStatus.setText("Importando modelo… mantenha o aplicativo aberto.");
            new Thread(()->{
                try(InputStream in=getContentResolver().openInputStream(uri); FileOutputStream out=new FileOutputStream(new File(getFilesDir(),"gemma-3-1b-it-Q4_K_M.gguf"))){
                    if(in==null) throw new IOException("Não foi possível abrir o arquivo selecionado.");
                    byte[] buf=new byte[1024*1024]; int n; while((n=in.read(buf))>0)out.write(buf,0,n);
                    runOnUiThread(()->modelStatus.setText("Modelo importado. Volte à tela principal para carregar."));
                }catch(Exception e){ runOnUiThread(()->modelStatus.setText("Erro ao importar: "+e.getMessage())); }
            }).start();
        }
    }
}
