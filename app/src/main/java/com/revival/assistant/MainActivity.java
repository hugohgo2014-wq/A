package com.revival.assistant;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.widget.*;
import java.io.File;
import java.util.*;
import java.util.regex.*;

public class MainActivity extends Activity {
    private LinearLayout root, messages; private EditText input; private TextView status; private TextToSpeech tts; private SpeechRecognizer speech; private boolean busy=false; private boolean uiGl=false; private boolean uiCreated=false; private boolean torchOn=false; private boolean modelLoading=false;
    @Override public void onCreate(Bundle b){ super.onCreate(b); tts=new TextToSpeech(this, s->{ if(s==TextToSpeech.SUCCESS) tts.setLanguage(new Locale("pt","BR")); }); showUi(); }
    private void showUi(){
        boolean gl=getSharedPreferences("assistant_settings",MODE_PRIVATE).getBoolean("use_gl",false); uiGl=gl; uiCreated=true;
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xff10131a);
        if(gl){ GlSurface surface=new GlSurface(this); root.addView(surface,new LinearLayout.LayoutParams(-1,0,1)); }
        else { TextView header=new TextView(this); header.setText("ASSISTANT REVIVAL"); header.setTextColor(0xff8ab4f8); header.setTextSize(22); header.setGravity(Gravity.CENTER); header.setPadding(12,36,12,18); root.addView(header); }
        status=new TextView(this); status.setTextColor(0xffaaaaaa); status.setText("Assistente local • Gemma 3 1B"); status.setPadding(16,8,16,8); root.addView(status);
        ScrollView scroll=new ScrollView(this); messages=new LinearLayout(this); messages.setOrientation(LinearLayout.VERTICAL); messages.setPadding(16,8,16,8); scroll.addView(messages); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,2));
        LinearLayout row=new LinearLayout(this); row.setPadding(8,8,8,8); input=new EditText(this); input.setSingleLine(true); input.setHint("Fale ou digite um comando"); input.setTextColor(Color.WHITE); input.setHintTextColor(0xff999999); row.addView(input,new LinearLayout.LayoutParams(0,-2,1));
        Button send=new Button(this); send.setText("Enviar"); send.setOnClickListener(v->sendText(input.getText().toString())); row.addView(send);
        Button mic=new Button(this); mic.setText("🎙"); mic.setOnClickListener(v->listen()); row.addView(mic);
        root.addView(row); Button settings=new Button(this); settings.setText("Configurações"); settings.setOnClickListener(v->startActivity(new Intent(this,SettingsActivity.class))); root.addView(settings); setContentView(root);
        ensureModelLoaded();
        addMessage("Assistente", "Olá. Posso conversar localmente e executar alguns comandos Android.");
    }
    private void addMessage(String who,String text){ TextView v=new TextView(this); v.setText(who+": "+text); v.setTextColor(Color.WHITE); v.setTextSize(16); v.setPadding(10,10,10,10); messages.addView(v); }
    private void sendText(String text){ if(text==null||text.trim().isEmpty()||busy)return; input.setText(""); addMessage("Você",text); if(handleCommand(text))return; if(!NativeBridge.isLoaded()){ String msg="O modelo ainda não está carregado. Importe o GGUF nas Configurações."; addMessage("Assistente",msg); speak(msg); return; } busy=true; status.setText("Pensando localmente…"); new Thread(()->{ String prompt="<start_of_turn>user\nResponda em português brasileiro, de forma objetiva. Se o usuário pedir uma ação no Android, explique que comandos de ação são executados pelo aplicativo e não invente que executou.\n"+text+"<end_of_turn>\n<start_of_turn>model\n"; String answer=NativeBridge.generate(prompt); runOnUiThread(()->{ busy=false; status.setText("Gemma local • pronto"); addMessage("Assistente",answer); speak(answer); }); }).start(); }
    private boolean handleCommand(String text){ String s=text.toLowerCase(new Locale("pt","BR")).trim();
        if(s.matches(".*(que horas|hora são|horas são).*")){ String r=java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT,new Locale("pt","BR")).format(new Date()); addMessage("Assistente","Agora são "+r); speak("Agora são "+r); return true; }
        if(s.contains("lanterna")&&(s.contains("lig")||s.contains("deslig"))){ toggleTorch(); return true; }
        if(s.startsWith("abrir ")){ String name=s.substring(6).trim(); PackageManager pm=getPackageManager(); Intent launch=null; Intent query=new Intent(Intent.ACTION_MAIN); query.addCategory(Intent.CATEGORY_LAUNCHER); List<android.content.pm.ResolveInfo> apps=pm.queryIntentActivities(query,0); for(android.content.pm.ResolveInfo info:apps){ String label=info.loadLabel(pm).toString().toLowerCase(new Locale("pt","BR")); if(label.equals(name)||label.contains(name)){ launch=pm.getLaunchIntentForPackage(info.activityInfo.packageName); if(launch!=null)break; } } if(launch!=null){ startActivity(launch); addMessage("Assistente","Abrindo "+name+"."); } else addMessage("Assistente","Não encontrei o aplicativo "+name+"."); return true; }
        if(s.contains("aumentar volume")||s.contains("diminuir volume")){ android.media.AudioManager am=(android.media.AudioManager)getSystemService(AUDIO_SERVICE); int dir=s.contains("aumentar")?android.media.AudioManager.ADJUST_RAISE:android.media.AudioManager.ADJUST_LOWER; am.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,dir,android.media.AudioManager.FLAG_SHOW_UI); addMessage("Assistente","Volume ajustado."); return true; }
        if(s.contains("alarme")&&s.contains("criar")){ Intent i=new Intent(android.provider.AlarmClock.ACTION_SET_ALARM); i.putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI,false); if(i.resolveActivity(getPackageManager())!=null)startActivity(i); addMessage("Assistente","Abrindo a criação de alarme."); return true; }
        return false;
    }
    private void listen(){ if(!SpeechRecognizer.isRecognitionAvailable(this)){ addMessage("Assistente","Reconhecimento de voz indisponível neste aparelho."); return; } if(checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){ requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO},7); return; } if(speech!=null)speech.destroy(); speech=SpeechRecognizer.createSpeechRecognizer(this); speech.setRecognitionListener(new RecognitionListener(){ public void onReadyForSpeech(Bundle b){status.setText("Ouvindo…");} public void onBeginningOfSpeech(){} public void onRmsChanged(float v){} public void onBufferReceived(byte[] b){} public void onEndOfSpeech(){status.setText("Processando voz…");} public void onError(int e){status.setText("Não foi possível reconhecer a fala.");} public void onResults(Bundle b){ArrayList<String> r=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION); if(r!=null&&!r.isEmpty())sendText(r.get(0));} public void onPartialResults(Bundle b){} public void onEvent(int t,Bundle b){} }); Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"pt-BR"); speech.startListening(i); }
    private void toggleTorch(){
        if(checkSelfPermission(android.Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){ requestPermissions(new String[]{android.Manifest.permission.CAMERA},8); addMessage("Assistente","Preciso da permissão da câmera para controlar a lanterna."); return; }
        try { android.hardware.camera2.CameraManager cm=(android.hardware.camera2.CameraManager)getSystemService(CAMERA_SERVICE); String id=null; for(String cameraId:cm.getCameraIdList()){ android.hardware.camera2.CameraCharacteristics c=cm.getCameraCharacteristics(cameraId); Boolean flash=c.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE); Integer facing=c.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING); if(Boolean.TRUE.equals(flash) && (facing==null || facing==android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK)){ id=cameraId; break; } } if(id==null){ addMessage("Assistente","Este aparelho não informou uma lanterna disponível."); return; } torchOn=!torchOn; cm.setTorchMode(id,torchOn); String msg=torchOn?"Lanterna ligada.":"Lanterna desligada."; addMessage("Assistente",msg); speak(msg); } catch(Exception e){ addMessage("Assistente","Não foi possível controlar a lanterna: "+e.getMessage()); }
    }
    private void ensureModelLoaded(){
        File modelFile=new File(getFilesDir(),"gemma-3-1b-it-Q4_K_M.gguf");
        if(modelLoading) return;
        modelLoading=true;
        new Thread(()->{
            boolean ok=false;
            try {
                if(!modelFile.exists()) {
                    try(java.io.InputStream in=getAssets().open("models/gemma-3-1b-it-Q4_K_M.gguf"); java.io.FileOutputStream out=new java.io.FileOutputStream(modelFile)) {
                        byte[] buffer=new byte[1024*1024]; int count; while((count=in.read(buffer))!=-1) out.write(buffer,0,count);
                    } catch(Exception noBundledModel) { /* User can import it from Settings. */ }
                }
                ok=modelFile.exists() && (NativeBridge.isLoaded() || NativeBridge.loadModel(modelFile.getAbsolutePath()));
            }
            catch(Throwable t){ ok=false; }
            final boolean loaded=ok;
            runOnUiThread(()->{ modelLoading=false; status.setText(loaded?"Gemma carregado • offline":(modelFile.exists()?"Falha ao carregar o modelo GGUF":"Importe o Gemma GGUF em Configurações")); });
        }).start();
    }
    private void speak(String text){ if(tts!=null)tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"assistant_reply"); }
    @Override protected void onDestroy(){ if(speech!=null)speech.destroy(); if(tts!=null)tts.shutdown(); super.onDestroy(); }
    @Override protected void onResume(){ super.onResume(); if(uiCreated){ boolean wanted=getSharedPreferences("assistant_settings",MODE_PRIVATE).getBoolean("use_gl",false); if(wanted!=uiGl) showUi(); ensureModelLoaded(); } }
}
