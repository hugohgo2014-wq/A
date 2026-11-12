package studio.renpy.mobile;

import android.app.*;
import android.os.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class MainActivity extends Activity {
 private final int BG=0xff101018,PANEL=0xff1b1a29,PANEL2=0xff242236,PURPLE=0xffa995ff,MINT=0xff74dfc2,TEXT=0xfff4f1ff,MUTED=0xffa5a1b7;
 private LinearLayout root,content,toolbar,bottom; private FrameLayout stage;
 private JSONArray events=new JSONArray(); private ArrayList<String> assets=new ArrayList<>();
 private String projectName="Minha Visual Novel",projectId="minha_visual_novel"; private int tab=0,subtab=0;
 private final String[] tabs={"Studio","Assets","Preview","Exportar"}; private final String[] subs={"Cena","Eventos","Personagens"};
 private static final int PICK_ASSET=41,EXPORT_ZIP=42;

 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);loadProject();buildShell();showScreen();}
 private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
 private GradientDrawable shape(int color,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
 private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(android.graphics.Typeface.DEFAULT,1);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
 private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
 private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 private void pad(View v,int a){v.setPadding(dp(a),dp(a),dp(a),dp(a));}
 private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
 private TextView button(String s,Runnable r,boolean primary){TextView b=text(s,14,primary?BG:TEXT,true);b.setGravity(Gravity.CENTER);pad(b,12);b.setBackground(shape(primary?PURPLE:PANEL2,14,primary?0:0xff39364f));b.setOnClickListener(v->r.run());return b;}
 private void loadProject(){try{events=new JSONArray(getPreferences(0).getString("events","[]"));}catch(Exception e){events=new JSONArray();}projectName=getPreferences(0).getString("name","Minha Visual Novel");try{JSONArray a=new JSONArray(getPreferences(0).getString("assets","[]"));for(int i=0;i<a.length();i++)assets.add(a.getString(i));}catch(Exception ignored){}}
 private void saveProject(){getPreferences(0).edit().putString("events",events.toString()).putString("name",projectName).putString("assets",new JSONArray(assets).toString()).apply();}
 private void buildShell(){root=column();root.setBackgroundColor(BG);setContentView(root);toolbar=row();pad(toolbar,16);root.addView(toolbar,lp(-1,60));ScrollView sc=new ScrollView(this);sc.setFillViewport(true);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));content=column();pad(content,16);sc.addView(content);bottom=row();bottom.setBackgroundColor(0xff171624);bottom.setPadding(dp(4),dp(7),dp(4),dp(7));root.addView(bottom,lp(-1,66));for(int i=0;i<tabs.length;i++){final int k=i;LinearLayout item=column();item.setGravity(Gravity.CENTER);TextView ic=text(new String[]{"▦","▧","▷","⇧"}[i],19,i==tab?PURPLE:MUTED,true);ic.setGravity(Gravity.CENTER);TextView la=text(tabs[i],10,i==tab?PURPLE:MUTED,true);la.setGravity(Gravity.CENTER);item.addView(ic);item.addView(la);item.setOnClickListener(v->{tab=k;showScreen();});bottom.addView(item,new LinearLayout.LayoutParams(0,-1,1));}}
 private void showScreen(){content.removeAllViews();toolbar.removeAllViews();TextView brand=text("✦  REN'PY STUDIO",15,TEXT,true);toolbar.addView(brand,new LinearLayout.LayoutParams(0,dp(40),1));TextView proj=text(projectName+"  ▾",12,MUTED,false);proj.setOnClickListener(v->renameProject());toolbar.addView(proj);if(tab==0)studio();else if(tab==1)assetScreen();else if(tab==2)previewScreen();else exportScreen();for(int i=0;i<bottom.getChildCount();i++){LinearLayout it=(LinearLayout)bottom.getChildAt(i);it.getChildAt(0).setAlpha(i==tab?1f:.5f);it.getChildAt(1).setAlpha(i==tab?1f:.5f);}}
 private void heading(String h,String s){content.addView(text(h,25,TEXT,true),lp(-1,38));content.addView(text(s,13,MUTED,false),lp(-1,-2));space(14);}
 private void space(int h){View v=new View(this);content.addView(v,lp(1,h));}
 private void spaceIn(LinearLayout l,int h){View v=new View(this);l.addView(v,lp(1,h));}
 private void card(View v){v.setBackground(shape(PANEL,18,0xff302d43));pad(v,14);LinearLayout.LayoutParams p=lp(-1,-2);p.bottomMargin=dp(10);content.addView(v,p);}
 private void studio(){heading("Seu estúdio","Crie cenas, personagens e lógica sem escrever código.");LinearLayout chips=row();for(int i=0;i<3;i++){final int k=i;TextView b=button(subs[i],()->{subtab=k;showScreen();},subtab==i);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(42),1);p.setMargins(0,0,dp(6),0);chips.addView(b,p);}content.addView(chips);space(14);if(subtab==0)sceneEditor();else if(subtab==1)eventEditor();else characterEditor();}
 private void sceneEditor(){content.addView(text("PRÉVIA DA CENA",11,MUTED,true));space(8);stage=new FrameLayout(this);stage.setBackground(shape(0xff29253b,18,0xff39344f));stage.setClipToOutline(true);LinearLayout.LayoutParams sp=lp(-1,245);sp.bottomMargin=dp(12);content.addView(stage,sp);renderStage(false);LinearLayout a=row();a.addView(button("＋ Fundo",()->addBackground(),true),new LinearLayout.LayoutParams(0,dp(46),1));a.addView(button("＋ Diálogo",()->addDialogue(),false),new LinearLayout.LayoutParams(0,dp(46),1));content.addView(a);space(10);a=row();a.addView(button("＋ Personagem",()->addSprite(),false),new LinearLayout.LayoutParams(0,dp(46),1));a.addView(button("Camadas PNG",()->addLayered(),false),new LinearLayout.LayoutParams(0,dp(46),1));content.addView(a);space(16);content.addView(text("Importe imagens em Assets e use o nome do arquivo ao criar fundos e personagens.",12,MUTED,false));}
 private void renderStage(boolean dialogue){if(stage==null)return;stage.removeAllViews();String bg=null;JSONObject character=null;for(int i=0;i<events.length();i++){JSONObject e=events.optJSONObject(i);if(e==null)continue;if(e.optString("type").equals("background"))bg=e.optString("file");if(e.optString("type").equals("sprite")||e.optString("type").equals("layered"))character=e;}if(bg!=null)addImage(stage,bg);else{TextView empty=text("CENA 01  •  SEM FUNDO",11,0xff77718e,true);empty.setGravity(Gravity.CENTER);stage.addView(empty,new FrameLayout.LayoutParams(-1,-1));}if(character!=null){if(character.optString("type").equals("sprite"))addImage(stage,character.optString("file"));else{addImage(stage,character.optString("body"));addImage(stage,character.optString("eyes"));addImage(stage,character.optString("mouth"));}}if(dialogue){LinearLayout box=column();box.setBackground(shape(0xee171624,12,0xff8172bd));pad(box,10);box.addView(text("PERSONAGEM",10,MINT,true));box.addView(text("A história começa aqui...",13,TEXT,false));FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,dp(76),Gravity.BOTTOM);p.setMargins(dp(8),0,dp(8),dp(8));stage.addView(box,p);}}
 private void addImage(FrameLayout parent,String file){addImage(parent,file,0.5f,0.5f,1.0f,0.0f);}
 private void addImage(FrameLayout parent,String file,float x,float y,float zoom,float rotation){
  if(file==null||file.isEmpty())return;File f=new File(getFilesDir(),"assets/"+file);if(!f.exists())return;
  try{
   android.widget.ImageView im=new android.widget.ImageView(this);im.setImageURI(Uri.fromFile(f));im.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
   parent.addView(im,new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER));im.setScaleX(zoom);im.setScaleY(zoom);im.setRotation(rotation);
   parent.post(()->{im.setTranslationX((x-0.5f)*parent.getWidth());im.setTranslationY((y-0.5f)*parent.getHeight());});
  }catch(Exception ignored){}
 }
 private void renderStage(boolean dialogue){
  if(stage==null)return;stage.removeAllViews();JSONObject background=null;LinkedHashMap<String,JSONObject> active=new LinkedHashMap<>();
  for(int i=0;i<events.length();i++){
   JSONObject e=events.optJSONObject(i);if(e==null)continue;String type=e.optString("type"),name=e.optString("name");
   if(type.equals("background"))background=e;
   else if(type.equals("sprite")||type.equals("layered"))active.put(name.isEmpty()?type+"_"+i:name,e);
   else if(type.equals("hide"))active.remove(name);
  }
  if(background!=null)addImage(stage,background.optString("file"),floatValue(background,"x",0.5f),floatValue(background,"y",0.5f),floatValue(background,"zoom",1),floatValue(background,"rotation",0));
  else{TextView empty=text("CENA • SEM FUNDO",11,0xff77718e,true);empty.setGravity(Gravity.CENTER);stage.addView(empty,new FrameLayout.LayoutParams(-1,-1));}
  for(JSONObject e:active.values()){
   float x=floatValue(e,"x",0.5f),y=floatValue(e,"y",0.62f),z=floatValue(e,"zoom",0.9f),r=floatValue(e,"rotation",0);
   if(e.optString("type").equals("sprite"))addImage(stage,e.optString("file"),x,y,z,r);
   else{addImage(stage,e.optString("left"),x,y,z,r);addImage(stage,e.optString("right"),x,y,z,r);addImage(stage,e.optString("head"),x,y,z,r);}
  }
  if(dialogue){LinearLayout box=column();box.setBackground(shape(0xee171624,12,0xff8172bd));pad(box,10);box.addView(text("PERSONAGEM",10,MINT,true));box.addView(text("A história começa aqui...",13,TEXT,false));FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,dp(76),Gravity.BOTTOM);p.setMargins(dp(8),0,dp(8),dp(8));stage.addView(box,p);}
 }
 
 private void eventEditor(){heading("Folha de eventos","Condições + ações. Os blocos são executados de cima para baixo.");content.addView(button("＋ Adicionar evento",()->eventPicker(),true),lp(-1,48));space(12);if(events.length()==0){LinearLayout empty=column();empty.setGravity(Gravity.CENTER);empty.addView(text("◇",36,PURPLE,true));empty.addView(text("Nenhum evento ainda",16,TEXT,true));empty.addView(text("Adicione diálogos, condições e ações.",12,MUTED,false));card(empty);return;}for(int i=0;i<events.length();i++){final int ix=i;JSONObject e=events.optJSONObject(i);if(e==null)continue;LinearLayout block=column(),top=row();top.addView(text(eventLabel(e),13,PURPLE,true),new LinearLayout.LayoutParams(0,dp(28),1));TextView more=text("⋮",22,MUTED,true);more.setGravity(Gravity.CENTER);more.setOnClickListener(v->new AlertDialog.Builder(this).setItems(new String[]{"Mover para cima","Mover para baixo","Excluir"},(d,w)->{if(w==2)removeEvent(ix);else moveEvent(ix,w==0?-1:1);}).show());top.addView(more);block.addView(top);block.addView(text(eventDetail(e),13,TEXT,false));card(block);}content.addView(text("EVENTOS: "+events.length()+"  •  SALVAMENTO AUTOMÁTICO",10,MUTED,true));}
 private String eventLabel(JSONObject e){
  switch(e.optString("type")){
   case"say":return"DIÁLOGO";case"background":return"FUNDO";case"sprite":return"PERSONAGEM";case"layered":return"PERSONAGEM 3 PARTES";
   case"if":return"SE / CONDIÇÃO";case"else":return"SENÃO";case"endif":return"FIM DA CONDIÇÃO";case"set":return"DEFINIR VARIÁVEL";
   case"choice":return"ESCOLHA";case"music":return"TOCAR MÚSICA";case"sound":return"EFEITO SONORO";case"stopmusic":return"PARAR MÚSICA";
   case"transition":return"TRANSIÇÃO";case"pause":return"ESPERAR";case"hide":return"OCULTAR";case"label":return"RÓTULO";case"jump":return"PULAR PARA";case"call":return"CHAMAR";case"return":return"RETORNAR";case"end":return"FIM DA HISTÓRIA";default:return e.optString("type").toUpperCase(Locale.ROOT);
  }
 }
 private String eventDetail(JSONObject e){
  String t=e.optString("type");
  if(t.equals("say"))return(e.optString("speaker").isEmpty()?"Narrador":e.optString("speaker"))+": "+e.optString("text");
  if(t.equals("background"))return e.optString("file")+" • X "+e.optString("x","0.5")+" • Y "+e.optString("y","0.5")+" • Zoom "+e.optString("zoom","1");
  if(t.equals("sprite"))return e.optString("name")+" • "+e.optString("file")+" • X "+e.optString("x","0.5")+" • Y "+e.optString("y","0.62");
  if(t.equals("layered"))return e.optString("name")+" • Esquerda: "+e.optString("left")+" • Direita: "+e.optString("right")+" • Cabeça: "+e.optString("head");
  if(t.equals("if"))return e.optString("variable")+" "+e.optString("operator","==")+" "+e.optString("value");
  if(t.equals("set"))return e.optString("variable")+" = "+e.optString("value");
  if(t.equals("choice"))return"A) "+e.optString("a")+"  /  B) "+e.optString("b");
  if(t.equals("label")||t.equals("jump")||t.equals("call"))return e.optString("target");
  if(t.equals("transition"))return e.optString("effect");
  return e.optString("file",e.optString("value",""));
 }
 private void removeEvent(int i){JSONArray n=new JSONArray();for(int j=0;j<events.length();j++)if(j!=i)n.put(events.opt(j));events=n;saveProject();showScreen();}
 private void moveEvent(int i,int d){int j=i+d;if(j<0||j>=events.length())return;Object a=events.opt(i),b=events.opt(j);try{events.put(j,a);events.put(i,b);saveProject();showScreen();}catch(JSONException ex){Toast.makeText(this,"Não foi possível mover o evento: "+ex.getMessage(),Toast.LENGTH_LONG).show();}}
 private void addEvent(JSONObject e){events.put(e);saveProject();showScreen();}
 private void eventPicker(){String[] names={"Diálogo","Alterar fundo","Personagem (imagem única)","Personagem em 3 PNGs","Condição: se variável","Senão","Fim da condição","Definir variável","Escolha (2 opções)","Tocar música","Tocar efeito sonoro","Transição","Esperar","Ocultar personagem","Finalizar história"};new AlertDialog.Builder(this).setTitle("Adicionar bloco").setItems(names,(d,w)->{switch(w){case 0:addDialogue();break;case 1:addBackground();break;case 2:addSprite();break;case 3:addLayered();break;case 4:addIf();break;case 5:addSimple("else");break;case 6:addSimple("endif");break;case 7:addSet();break;case 8:addChoice();break;case 9:addAudio("music");break;case 10:addAudio("sound");break;case 11:addTransition();break;case 12:addPause();break;case 13:addHide();break;case 14:addSimple("end");break;}}).show();}
 private LinearLayout form(){LinearLayout l=column();pad(l,4);return l;}
 private EditText field(String hint){EditText e=new EditText(this);e.setHint(hint);e.setTextColor(TEXT);e.setHintTextColor(MUTED);e.setTextSize(14);e.setPadding(dp(12),dp(10),dp(12),dp(10));e.setBackground(shape(PANEL2,12,0xff39364f));LinearLayout.LayoutParams p=lp(-1,-2);p.bottomMargin=dp(8);e.setLayoutParams(p);return e;}
 private void formDialog(String title,LinearLayout f,Runnable save){new AlertDialog.Builder(this).setTitle(title).setView(f).setNegativeButton("Cancelar",null).setPositiveButton("Adicionar",(d,w)->save.run()).show();}
 private void addDialogue(){LinearLayout f=form();EditText s=field("Nome do personagem (vazio = narrador)"),t=field("Texto do diálogo");t.setMinLines(2);f.addView(s);f.addView(t);formDialog("Novo diálogo",f,()->{JSONObject e=new JSONObject();put(e,"type","say");put(e,"speaker",s.getText().toString());put(e,"text",t.getText().toString());addEvent(e);});}
 private void assetDialog(String title,java.util.function.Consumer<String> done){if(assets.isEmpty()){new AlertDialog.Builder(this).setMessage("Importe primeiro um arquivo na aba Assets.").setPositiveButton("Abrir Assets",(d,w)->{tab=1;showScreen();}).setNegativeButton("Cancelar",null).show();return;}String[] a=assets.toArray(new String[0]);new AlertDialog.Builder(this).setTitle(title).setItems(a,(d,w)->done.accept(a[w])).show();}
 private void assetDialogMatching(String title,String[] extensions,java.util.function.Consumer<String> done){
  ArrayList<String> matches=new ArrayList<>();
  for(String a:assets){String lower=a.toLowerCase(Locale.ROOT);for(String ext:extensions)if(lower.endsWith(ext)){matches.add(a);break;}}
  if(matches.isEmpty()){new AlertDialog.Builder(this).setTitle(title).setMessage("Não há arquivo compatível em Assets. Importe a imagem primeiro.").setPositiveButton("Abrir Assets",(d,w)->{tab=1;showScreen();}).setNegativeButton("Cancelar",null).show();return;}
  String[] items=matches.toArray(new String[0]);
  new AlertDialog.Builder(this).setTitle(title).setItems(items,(d,w)->done.accept(items[w])).show();
 }
 private SeekBar addSlider(LinearLayout parent,String label,int max,int progress){
  parent.addView(text(label,12,MUTED,true),lp(-1,26));
  SeekBar s=new SeekBar(this);s.setMax(max);s.setProgress(Math.max(0,Math.min(max,progress)));parent.addView(s,lp(-1,40));return s;
 }
 private void showTransformDialog(String title,JSONObject e,int editIndex,float x0,float y0,float z0,float r0){
  LinearLayout f=form();
  SeekBar x=addSlider(f,"Posição X • esquerda ↔ direita",1000,(int)(x0*1000));
  SeekBar y=addSlider(f,"Posição Y • cima ↔ baixo",1000,(int)(y0*1000));
  SeekBar z=addSlider(f,"Escala • 25% a 200%",175,Math.max(0,Math.min(175,(int)(z0*100)-25)));
  SeekBar r=addSlider(f,"Rotação • -180° a 180°",360,(int)(r0+180));
  formDialog(title,f,()->{
   put(e,"x",String.format(Locale.US,"%.3f",x.getProgress()/1000.0));
   put(e,"y",String.format(Locale.US,"%.3f",y.getProgress()/1000.0));
   put(e,"zoom",String.format(Locale.US,"%.2f",(z.getProgress()+25)/100.0));
   put(e,"rotation",Integer.toString(r.getProgress()-180));
   if(editIndex<0)addEvent(e);else try{events.put(editIndex,e);saveProject();showScreen();}catch(JSONException ex){toast("Erro ao salvar posição: "+ex.getMessage());}
  });
 }
 private float floatValue(JSONObject o,String key,float fallback){try{return (float)o.getDouble(key);}catch(Exception e){return fallback;}}
 private void addBackground(){
  assetDialogMatching("Escolher imagem de fundo",new String[]{".png",".jpg",".jpeg",".webp"},file->{
   JSONObject e=new JSONObject();put(e,"type","background");put(e,"file",file);
   showTransformDialog("Ajustar fundo",e,-1,0.5f,0.5f,1.0f,0.0f);
  });
 }
 private void addSprite(){
  LinearLayout f=form();EditText n=field("Nome do personagem (ex.: monika)");f.addView(n);
  formDialog("Novo personagem",f,()->{
   String name=n.getText().toString().trim();if(name.isEmpty())name="personagem";
   final String tag=name;
   assetDialogMatching("Selecionar sprite na biblioteca Assets",new String[]{".png",".jpg",".jpeg",".webp"},file->{
    JSONObject e=new JSONObject();put(e,"type","sprite");put(e,"name",tag);put(e,"file",file);
    showTransformDialog("Posição e tamanho do personagem",e,-1,0.5f,0.62f,0.9f,0.0f);
   });
  });
 }
 private void addLayered(){
  LinearLayout f=form();EditText n=field("Nome do personagem (ex.: sayori)");f.addView(n);
  formDialog("Personagem em 3 PNGs",f,()->{
   String name=n.getText().toString().trim();if(name.isEmpty())name="personagem";
   final String tag=name;
   assetDialogMatching("PNG 1/3 — lado esquerdo do corpo",new String[]{".png"},left->
    assetDialogMatching("PNG 2/3 — lado direito do corpo",new String[]{".png"},right->
     assetDialogMatching("PNG 3/3 — cabeça (fica por cima)",new String[]{".png"},head->{
      JSONObject e=new JSONObject();put(e,"type","layered");put(e,"name",tag);put(e,"left",left);put(e,"right",right);put(e,"head",head);
      showTransformDialog("Posição e tamanho do personagem",e,-1,0.5f,0.62f,0.9f,0.0f);
     })
    )
   );
  });
 }
 private void addIf(){LinearLayout f=form();EditText v=field("Variável (ex.: encontrou_carta)"),x=field("Valor esperado (True, False ou texto entre aspas)");x.setText("True");f.addView(v);f.addView(x);formDialog("Condição • SE",f,()->{JSONObject e=new JSONObject();put(e,"type","if");put(e,"variable",v.getText().toString());put(e,"value",x.getText().toString());addEvent(e);});}
 private void addSet(){LinearLayout f=form();EditText v=field("Nome da variável"),x=field("Valor (True, False, número ou texto)");f.addView(v);f.addView(x);formDialog("Definir variável",f,()->{JSONObject e=new JSONObject();put(e,"type","set");put(e,"variable",v.getText().toString());put(e,"value",x.getText().toString());addEvent(e);});}
 private void addChoice(){LinearLayout f=form();EditText a=field("Opção A"),b=field("Opção B");f.addView(a);f.addView(b);formDialog("Escolha do jogador",f,()->{JSONObject e=new JSONObject();put(e,"type","choice");put(e,"a",a.getText().toString());put(e,"b",b.getText().toString());addEvent(e);});}
 private void addAudio(String type){assetDialog("Escolher áudio",file->{JSONObject e=new JSONObject();put(e,"type",type);put(e,"file",file);addEvent(e);});}
 private void addTransition(){String[] a={"dissolve","fade","pixellate","move"};new AlertDialog.Builder(this).setTitle("Transição").setItems(a,(d,w)->{JSONObject e=new JSONObject();put(e,"type","transition");put(e,"effect",a[w]);addEvent(e);}).show();}
 private void addPause(){LinearLayout f=form();EditText x=field("Duração em segundos");x.setText("1.0");f.addView(x);formDialog("Esperar",f,()->{JSONObject e=new JSONObject();put(e,"type","pause");put(e,"value",x.getText().toString());addEvent(e);});}
 private void addHide(){LinearLayout f=form();EditText x=field("Identificador do personagem");f.addView(x);formDialog("Ocultar personagem",f,()->{JSONObject e=new JSONObject();put(e,"type","hide");put(e,"name",x.getText().toString());addEvent(e);});}
 private void addSimple(String t){JSONObject e=new JSONObject();put(e,"type",t);addEvent(e);}
 private void characterEditor(){
  heading("Personagens","Um PNG ou composição de três PNGs transparentes: lado esquerdo, lado direito e cabeça.");
  content.addView(button("＋ Personagem Doki-Doki (3 PNGs)",this::addLayered,true),lp(-1,48));space(8);
  content.addView(button("＋ Personagem com um PNG",this::addSprite,false),lp(-1,46));space(14);
  for(int i=0;i<events.length();i++){
   final int ix=i;JSONObject e=events.optJSONObject(i);if(e==null)continue;
   String t=e.optString("type");
   if(t.equals("sprite")||t.equals("layered")){
    LinearLayout c=column();c.addView(text(e.optString("name"),16,TEXT,true));c.addView(text(eventDetail(e),12,MUTED,false));
    c.setOnClickListener(v->editVisualEvent(ix));card(c);
   }
  }
 }
 private void editVisualEvent(int index){
  JSONObject e=events.optJSONObject(index);if(e==null)return;String type=e.optString("type");
  if(type.equals("sprite")){
   assetDialogMatching("Trocar PNG do personagem",new String[]{".png",".jpg",".jpeg",".webp"},file->{
    put(e,"file",file);
    showTransformDialog("Editar posição e tamanho",e,index,floatValue(e,"x",0.5f),floatValue(e,"y",0.62f),floatValue(e,"zoom",0.9f),floatValue(e,"rotation",0));
   });
  }else if(type.equals("layered")){
   assetDialogMatching("PNG 1/3 — lado esquerdo do corpo",new String[]{".png"},left->
    assetDialogMatching("PNG 2/3 — lado direito do corpo",new String[]{".png"},right->
     assetDialogMatching("PNG 3/3 — cabeça por cima",new String[]{".png"},head->{
      put(e,"left",left);put(e,"right",right);put(e,"head",head);
      showTransformDialog("Editar composição e posição",e,index,floatValue(e,"x",0.5f),floatValue(e,"y",0.62f),floatValue(e,"zoom",0.9f),floatValue(e,"rotation",0));
     })
    )
   );
  }else if(type.equals("background")){
   assetDialogMatching("Trocar fundo",new String[]{".png",".jpg",".jpeg",".webp"},file->{
    put(e,"file",file);
    showTransformDialog("Editar enquadramento do fundo",e,index,floatValue(e,"x",0.5f),floatValue(e,"y",0.5f),floatValue(e,"zoom",1),floatValue(e,"rotation",0));
   });
  }
 }
 private void assetScreen(){heading("Biblioteca de assets","Imagens PNG/JPG, músicas e efeitos sonoros.");content.addView(button("＋ Importar arquivo do dispositivo",this::pickAsset,true),lp(-1,48));space(14);if(assets.isEmpty())content.addView(text("Sua biblioteca está vazia. Importe sprites, fundos e áudio.",13,MUTED,false));for(String a:new ArrayList<>(assets)){LinearLayout r=row();r.addView(text("▧   "+a,13,TEXT,true),new LinearLayout.LayoutParams(0,dp(44),1));TextView del=text("Remover",11,0xffff8c9b,true);del.setOnClickListener(v->{assets.remove(a);new File(getFilesDir(),"assets/"+a).delete();saveProject();showScreen();});r.addView(del);card(r);}}
 private void pickAsset(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_ASSET);}
 @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(c!=RESULT_OK||d==null||d.getData()==null)return;if(r==PICK_ASSET)importAsset(d.getData());else if(r==EXPORT_ZIP)exportZip(d.getData());}
 private void importAsset(Uri u){try{String name="asset_"+System.currentTimeMillis();try(Cursor c=getContentResolver().query(u,null,null,null,null)){if(c!=null&&c.moveToFirst()){int ix=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(ix>=0)name=c.getString(ix);}}String safe=name.replaceAll("[^A-Za-z0-9._-]","_");File dir=new File(getFilesDir(),"assets");dir.mkdirs();try(InputStream in=getContentResolver().openInputStream(u);OutputStream out=new FileOutputStream(new File(dir,safe))){byte[] b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);}if(!assets.contains(safe))assets.add(safe);saveProject();showScreen();toast("Importado: "+safe);}catch(Exception e){toast("Falha ao importar: "+e.getMessage());}}
 private void previewScreen(){heading("Playtest","Pré-visualização da sequência de diálogos.");content.addView(button("▶  Executar do início",this::runPreview,true),lp(-1,50));space(12);stage=new FrameLayout(this);stage.setBackground(shape(PANEL2,18,0xff39364f));content.addView(stage,lp(-1,300));renderStage(true);space(12);content.addView(button("Abrir visualizador de diálogos",this::runPreview,false),lp(-1,46));}
 private int findConditionalBoundary(int from,boolean seekElse){int depth=1;for(int i=from;i<events.length();i++){JSONObject e=events.optJSONObject(i);if(e==null)continue;String t=e.optString("type");if(t.equals("if"))depth++;else if(t.equals("endif")){depth--;if(depth==0)return i+1;}else if(t.equals("else")&&depth==1&&seekElse)return i+1;}return events.length();}
 private void runPreview(){
  Dialog dlg=new Dialog(this);
  LinearLayout shell=column();shell.setBackgroundColor(BG);
  LinearLayout top=row();pad(top,14);top.addView(text("▶  PLAYTEST",14,PURPLE,true),new LinearLayout.LayoutParams(0,dp(44),1));
  TextView close=text("FECHAR  ✕",11,MUTED,true);close.setOnClickListener(v->dlg.dismiss());top.addView(close);shell.addView(top,lp(-1,50));
  FrameLayout playfield=new FrameLayout(this);playfield.setBackgroundColor(0xff28243a);shell.addView(playfield,new LinearLayout.LayoutParams(-1,0,1));
  final FrameLayout[] characters={new FrameLayout(this)};playfield.addView(characters[0],new FrameLayout.LayoutParams(-1,-1));
  LinearLayout dialogue=column();dialogue.setBackground(shape(0xff191827,18,0xff675b92));pad(dialogue,16);
  TextView who=text("NARRADOR",11,MINT,true),line=text("Preparando cena...",17,TEXT,false);dialogue.addView(who);spaceIn(dialogue,8);dialogue.addView(line);
  LinearLayout choices=row();dialogue.addView(choices);TextView next=button("Continuar  →",()->{},true);LinearLayout.LayoutParams np=lp(-1,46);np.topMargin=dp(12);dialogue.addView(next,np);
  LinearLayout.LayoutParams dpv=lp(-1,190);dpv.setMargins(dp(10),dp(8),dp(10),dp(12));shell.addView(dialogue,dpv);
  dlg.setContentView(shell);Window win=dlg.getWindow();if(win!=null){win.setBackgroundDrawableResource(android.R.color.transparent);win.setLayout(-1,-1);}
  final int[] cursor={0};final Map<String,String> vars=new HashMap<>();
  Runnable[] advance=new Runnable[1];
  advance[0]=()->{
   choices.removeAllViews();choices.setVisibility(View.GONE);next.setVisibility(View.VISIBLE);
   while(cursor[0]<events.length()){
    JSONObject ev=events.optJSONObject(cursor[0]++);if(ev==null)continue;String type=ev.optString("type");
    if(type.equals("background")){playfield.removeAllViews();playfield.setBackgroundColor(0xff28243a);characters[0]=new FrameLayout(this);playfield.addView(characters[0],new FrameLayout.LayoutParams(-1,-1));addImage(playfield,ev.optString("file"));playfield.bringChildToFront(characters[0]);}
    else if(type.equals("sprite")){characters[0].removeAllViews();addImage(characters[0],ev.optString("file"));}
    else if(type.equals("layered")){characters[0].removeAllViews();addImage(characters[0],ev.optString("body"));addImage(characters[0],ev.optString("eyes"));addImage(characters[0],ev.optString("mouth"));}
    else if(type.equals("hide"))characters[0].removeAllViews();
    else if(type.equals("set"))vars.put(ev.optString("variable"),ev.optString("value"));
    else if(type.equals("if")){String actual=vars.get(ev.optString("variable"));String expected=ev.optString("value").replace("\\\"","");if(actual==null||!actual.equalsIgnoreCase(expected))cursor[0]=findConditionalBoundary(cursor[0],true);}
    else if(type.equals("else")){cursor[0]=findConditionalBoundary(cursor[0],false);}
    else if(type.equals("say")){who.setText(ev.optString("speaker").isEmpty()?"NARRADOR":ev.optString("speaker").toUpperCase(Locale.ROOT));line.setText(ev.optString("text"));next.setOnClickListener(v->advance[0].run());return;}
    else if(type.equals("choice")){who.setText("ESCOLHA");line.setText("Escolha uma opção para continuar.");choices.setVisibility(View.VISIBLE);next.setVisibility(View.GONE);TextView a=button(ev.optString("a"),()->{vars.put("choice_result","A");choices.setVisibility(View.GONE);next.setVisibility(View.VISIBLE);next.setOnClickListener(v->advance[0].run());},true);TextView b=button(ev.optString("b"),()->{vars.put("choice_result","B");choices.setVisibility(View.GONE);next.setVisibility(View.VISIBLE);next.setOnClickListener(v->advance[0].run());},false);choices.addView(a,new LinearLayout.LayoutParams(0,dp(44),1));choices.addView(b,new LinearLayout.LayoutParams(0,dp(44),1));return;}
    else if(type.equals("end"))break;
   }
   who.setText("FIM DA HISTÓRIA");line.setText("Você chegou ao final desta prévia.");next.setText("Fechar");next.setOnClickListener(v->dlg.dismiss());
  };
  next.setOnClickListener(v->advance[0].run());dlg.show();if(dlg.getWindow()!=null)dlg.getWindow().setLayout(-1,-1);advance[0].run();
 }
 private void exportScreen(){heading("Exportar para Ren'Py","Gere um ZIP para extrair dentro da pasta game/ do projeto Ren'Py.");LinearLayout info=column();info.addView(text("PACOTE DE EXPORTAÇÃO",11,MINT,true));spaceIn(info,8);info.addView(text("• script.rpy compilado em C++\n• images/ com fundos e sprites\n• audio/ com músicas e efeitos\n• README com instruções",14,TEXT,false));card(info);content.addView(button("⇩  Gerar ZIP do projeto",()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/zip");i.putExtra(Intent.EXTRA_TITLE,projectId+"_renpy.zip");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,EXPORT_ZIP);},true),lp(-1,54));space(14);content.addView(text("O ZIP contém scripts e assets. Extraia na pasta game/ de um projeto Ren'Py existente. O APK não inclui o runtime Ren'Py.",12,MUTED,false));}
 private void exportZip(Uri target){try{StringBuilder input=new StringBuilder();for(int i=0;i<events.length();i++){JSONObject e=events.optJSONObject(i);if(e==null)continue;String t=e.optString("type");ArrayList<String> v=new ArrayList<>();switch(t){case"say":v.add(e.optString("speaker"));v.add(e.optString("text"));break;case"background":v.add(e.optString("file"));break;case"sprite":v.add(e.optString("name"));v.add(e.optString("file"));v.add(e.optString("position"));break;case"layered":v.add(e.optString("name"));v.add(e.optString("body"));v.add(e.optString("eyes"));v.add(e.optString("mouth"));v.add(e.optString("position"));break;case"if":case"set":v.add(e.optString("variable"));v.add(e.optString("value"));break;case"choice":v.add(e.optString("a"));v.add(e.optString("b"));break;case"music":case"sound":v.add(e.optString("file"));break;case"transition":v.add(e.optString("effect"));break;case"pause":v.add(e.optString("value"));break;case"hide":v.add(e.optString("name"));break;}input.append(t);for(String s:v)input.append("|").append(Base64.getEncoder().encodeToString(s.getBytes("UTF-8")));input.append("\n");}String script=NativeBridge.compileStory(input.toString());try(ZipOutputStream z=new ZipOutputStream(getContentResolver().openOutputStream(target))){putZip(z,"script.rpy",script);putZip(z,"README_RENPY_STUDIO.txt","Ren'Py Studio Mobile export\nExtract these files into an existing Ren'Py project's game/ directory.\n");for(String a:assets){File f=new File(getFilesDir(),"assets/"+a);String lower=a.toLowerCase(Locale.ROOT);String folder=(lower.endsWith(".ogg")||lower.endsWith(".mp3")||lower.endsWith(".wav"))?"audio/":"images/";try(InputStream in=new FileInputStream(f)){z.putNextEntry(new ZipEntry(folder+a));byte[] b=new byte[8192];int n;while((n=in.read(b))>0)z.write(b,0,n);z.closeEntry();}}}toast("ZIP exportado.");}catch(Exception e){toast("Erro na exportação: "+e.getMessage());}}
 private void putZip(ZipOutputStream z,String n,String s)throws Exception{z.putNextEntry(new ZipEntry(n));z.write(s.getBytes("UTF-8"));z.closeEntry();}
 private void renameProject(){LinearLayout f=form();EditText e=field("Nome do projeto");e.setText(projectName);f.addView(e);formDialog("Nome do projeto",f,()->{projectName=e.getText().toString().trim();if(projectName.isEmpty())projectName="Minha Visual Novel";projectId=projectName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","_");saveProject();showScreen();});}
 private void put(JSONObject o,String k,String v){try{o.put(k,v);}catch(Exception ignored){}}
 private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
