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
 private JSONArray events=new JSONArray(); private ArrayList<String> assets=new ArrayList<>(); private JSONObject guiSettings=new JSONObject(); private boolean includeRpaConfig=true,includeRpycHelper=true; private PluginManager pluginManager;
 private String projectName="Minha Visual Novel",projectId="minha_visual_novel"; private int tab=0,subtab=0;
 private final String[] tabs={"Studio","Assets","Aparência","Preview","Plugins","Exportar"}; private final String[] subs={"Cena","Eventos","Personagens"};
 private static final int PICK_ASSET=41,EXPORT_ZIP=42,PICK_PLUGIN=43;

 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);loadProject();pluginManager=new PluginManager(this,()->runOnUiThread(()->{if(tab==4)showScreen();}));buildShell();showScreen();}
 private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
 private GradientDrawable shape(int color,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
 private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(android.graphics.Typeface.DEFAULT,1);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
 private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
 private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 private void pad(View v,int a){v.setPadding(dp(a),dp(a),dp(a),dp(a));}
 private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
 private TextView button(String s,Runnable r,boolean primary){TextView b=text(s,14,primary?BG:TEXT,true);b.setGravity(Gravity.CENTER);pad(b,12);b.setBackground(shape(primary?PURPLE:PANEL2,14,primary?0:0xff39364f));b.setOnClickListener(v->r.run());return b;}
 private void loadProject(){try{events=new JSONArray(getPreferences(0).getString("events","[]"));}catch(Exception e){events=new JSONArray();}projectName=getPreferences(0).getString("name","Minha Visual Novel");try{JSONArray a=new JSONArray(getPreferences(0).getString("assets","[]"));for(int i=0;i<a.length();i++)assets.add(a.getString(i));}catch(Exception ignored){}try{guiSettings=new JSONObject(getPreferences(0).getString("gui","{}"));}catch(Exception ignored){guiSettings=new JSONObject();}includeRpaConfig=getPreferences(0).getBoolean("include_rpa",true);includeRpycHelper=getPreferences(0).getBoolean("include_rpyc",true);}
 private void saveProject(){getPreferences(0).edit().putString("events",events.toString()).putString("name",projectName).putString("assets",new JSONArray(assets).toString()).putString("gui",guiSettings.toString()).putBoolean("include_rpa",includeRpaConfig).putBoolean("include_rpyc",includeRpycHelper).apply();}
 private void buildShell(){root=column();root.setBackgroundColor(BG);setContentView(root);toolbar=row();pad(toolbar,16);root.addView(toolbar,lp(-1,60));ScrollView sc=new ScrollView(this);sc.setFillViewport(true);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));content=column();pad(content,16);sc.addView(content);bottom=row();bottom.setBackgroundColor(0xff171624);bottom.setPadding(dp(4),dp(7),dp(4),dp(7));root.addView(bottom,lp(-1,66));for(int i=0;i<tabs.length;i++){final int k=i;LinearLayout item=column();item.setGravity(Gravity.CENTER);TextView ic=text(new String[]{"▦","▧","⚙","▷","◇","⇧"}[i],19,i==tab?PURPLE:MUTED,true);ic.setGravity(Gravity.CENTER);TextView la=text(tabs[i],10,i==tab?PURPLE:MUTED,true);la.setGravity(Gravity.CENTER);item.addView(ic);item.addView(la);item.setOnClickListener(v->{tab=k;showScreen();});bottom.addView(item,new LinearLayout.LayoutParams(0,-1,1));}}
 private void showScreen(){content.removeAllViews();toolbar.removeAllViews();TextView brand=text("✦  REN'PY STUDIO",15,TEXT,true);toolbar.addView(brand,new LinearLayout.LayoutParams(0,dp(40),1));TextView proj=text(projectName+"  ▾",12,MUTED,false);proj.setOnClickListener(v->renameProject());toolbar.addView(proj);if(tab==0)studio();else if(tab==1)assetScreen();else if(tab==2)appearanceScreen();else if(tab==3)previewScreen();else if(tab==4)pluginScreen();else exportScreen();for(int i=0;i<bottom.getChildCount();i++){LinearLayout it=(LinearLayout)bottom.getChildAt(i);it.getChildAt(0).setAlpha(i==tab?1f:.5f);it.getChildAt(1).setAlpha(i==tab?1f:.5f);}}
 private void heading(String h,String s){content.addView(text(h,25,TEXT,true),lp(-1,38));content.addView(text(s,13,MUTED,false),lp(-1,-2));space(14);}
 private void space(int h){View v=new View(this);content.addView(v,lp(1,h));}
 private void spaceIn(LinearLayout l,int h){View v=new View(this);l.addView(v,lp(1,h));}
 private void card(View v){v.setBackground(shape(PANEL,18,0xff302d43));pad(v,14);LinearLayout.LayoutParams p=lp(-1,-2);p.bottomMargin=dp(10);content.addView(v,p);}
 private void studio(){heading("Seu estúdio","Crie cenas, personagens e lógica sem escrever código.");LinearLayout chips=row();for(int i=0;i<3;i++){final int k=i;TextView b=button(subs[i],()->{subtab=k;showScreen();},subtab==i);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(42),1);p.setMargins(0,0,dp(6),0);chips.addView(b,p);}content.addView(chips);space(14);if(subtab==0)sceneEditor();else if(subtab==1)eventEditor();else characterEditor();}
 private void sceneEditor(){content.addView(text("PRÉVIA DA CENA",11,MUTED,true));space(8);stage=new FrameLayout(this);stage.setBackground(shape(0xff29253b,18,0xff39344f));stage.setClipToOutline(true);LinearLayout.LayoutParams sp=lp(-1,245);sp.bottomMargin=dp(12);content.addView(stage,sp);renderStage(false);LinearLayout a=row();a.addView(button("＋ Fundo",()->addBackground(),true),new LinearLayout.LayoutParams(0,dp(46),1));a.addView(button("＋ Diálogo",()->addDialogue(),false),new LinearLayout.LayoutParams(0,dp(46),1));content.addView(a);space(10);a=row();a.addView(button("＋ Personagem",()->addSprite(),false),new LinearLayout.LayoutParams(0,dp(46),1));a.addView(button("Camadas PNG",()->addLayered(),false),new LinearLayout.LayoutParams(0,dp(46),1));content.addView(a);space(16);content.addView(text("Importe imagens em Assets e use o nome do arquivo ao criar fundos e personagens.",12,MUTED,false));}
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
 
 private void eventEditor(){
  heading("Folha de eventos","Adicione blocos visuais para diálogos, ações, condições e fluxo da história.");
  LinearLayout add=row();
  add.addView(button("＋ Ação",()->eventPicker(),true),new LinearLayout.LayoutParams(0,dp(44),1));
  add.addView(button("◇ Condição",()->addIf(),false),new LinearLayout.LayoutParams(0,dp(44),1));
  addViewSpace(add,6);
  content.addView(add);
  space(8);
  content.addView(button("＋ Rótulo / Pular / Chamar / Retornar",()->flowPicker(),false),lp(-1,42));
  space(12);
  if(events.length()==0){
   LinearLayout empty=column();empty.setGravity(Gravity.CENTER);
   empty.addView(text("◇",36,PURPLE,true));empty.addView(text("Folha vazia",16,TEXT,true));
   empty.addView(text("Comece adicionando uma ação ou condição.",12,MUTED,false));card(empty);return;
  }
  for(int i=0;i<events.length();i++){
   final int ix=i;JSONObject e=events.optJSONObject(i);if(e==null)continue;
   String type=e.optString("type");
   int railColor=(type.equals("if")||type.equals("else")||type.equals("endif"))?MINT:
      (type.equals("say")||type.equals("choice"))?PURPLE:0xff68a9ed;
   LinearLayout block=row(),body=column(),top=row();
   View rail=new View(this);rail.setBackground(shape(railColor,8,0));block.addView(rail,lp(5,60));
   LinearLayout.LayoutParams bodyParams=new LinearLayout.LayoutParams(0,-2,1);bodyParams.leftMargin=dp(10);block.addView(body,bodyParams);
   top.addView(text(String.format(Locale.ROOT,"%02d  %s",i+1,eventLabel(e)),12,railColor,true),new LinearLayout.LayoutParams(0,dp(28),1));
   TextView more=text("⋮",22,MUTED,true);more.setGravity(Gravity.CENTER);
   more.setOnClickListener(v->new AlertDialog.Builder(this).setItems(new String[]{"Editar bloco","Mover para cima","Mover para baixo","Duplicar","Excluir"},(d,w)->{
    if(w==0)editEvent(ix);else if(w==1)moveEvent(ix,-1);else if(w==2)moveEvent(ix,1);else if(w==3)duplicateEvent(ix);else removeEvent(ix);
   }).show());
   top.addView(more);body.addView(top);body.addView(text(eventDetail(e),13,TEXT,false));
   block.setOnClickListener(v->editEvent(ix));card(block);
  }
  content.addView(text("BLOCOS: "+events.length()+"  •  SALVAMENTO AUTOMÁTICO",10,MUTED,true));
 }
 private void addViewSpace(LinearLayout parent,int width){View v=new View(this);parent.addView(v,lp(width,1));}
 private void duplicateEvent(int index){
  JSONObject e=events.optJSONObject(index);if(e==null)return;
  try{JSONArray n=new JSONArray();for(int i=0;i<events.length();i++){n.put(events.opt(i));if(i==index)n.put(new JSONObject(e.toString()));}events=n;saveProject();showScreen();}
  catch(Exception ex){toast("Falha ao duplicar bloco: "+ex.getMessage());}
 }
 private void saveEditedEvent(int index,JSONObject e){
  try{events.put(index,e);saveProject();showScreen();}
  catch(JSONException ex){toast("Não foi possível salvar o bloco: "+ex.getMessage());}
 }
 private void editFormDialog(String title,LinearLayout f,Runnable save){
  new AlertDialog.Builder(this).setTitle(title).setView(f).setNegativeButton("Cancelar",null).setPositiveButton("Salvar",(d,w)->save.run()).show();
 }
 private void editEvent(int index){
  JSONObject e=events.optJSONObject(index);if(e==null)return;String type=e.optString("type");
  if(type.equals("sprite")||type.equals("layered")||type.equals("background")){editVisualEvent(index);return;}
  if(type.equals("say")){
   LinearLayout f=form();EditText speaker=field("Personagem / narrador"),line=field("Texto do diálogo");
   speaker.setText(e.optString("speaker"));line.setText(e.optString("text"));line.setMinLines(2);f.addView(speaker);f.addView(line);
   editFormDialog("Editar diálogo",f,()->{put(e,"speaker",speaker.getText().toString());put(e,"text",line.getText().toString());saveEditedEvent(index,e);});
  }else if(type.equals("if")){
   LinearLayout f=form();EditText variable=field("Variável");variable.setText(e.optString("variable"));
   EditText value=field("Valor esperado");value.setText(e.optString("value"));
   String[] ops={"==","!=",">","<",">=","<="};Spinner op=new Spinner(this);
   ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,ops);adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);op.setAdapter(adapter);
   for(int i=0;i<ops.length;i++)if(ops[i].equals(e.optString("operator","==")))op.setSelection(i);
   f.addView(variable);f.addView(op);f.addView(value);
   editFormDialog("Editar condição",f,()->{put(e,"variable",variable.getText().toString());put(e,"operator",ops[op.getSelectedItemPosition()]);put(e,"value",value.getText().toString());saveEditedEvent(index,e);});
  }else if(type.equals("set")){
   LinearLayout f=form();EditText variable=field("Variável"),value=field("Valor");variable.setText(e.optString("variable"));value.setText(e.optString("value"));f.addView(variable);f.addView(value);
   editFormDialog("Editar variável",f,()->{put(e,"variable",variable.getText().toString());put(e,"value",value.getText().toString());saveEditedEvent(index,e);});
  }else if(type.equals("choice")){
   LinearLayout f=form();EditText a=field("Opção A"),b=field("Opção B");a.setText(e.optString("a"));b.setText(e.optString("b"));f.addView(a);f.addView(b);
   editFormDialog("Editar escolha",f,()->{put(e,"a",a.getText().toString());put(e,"b",b.getText().toString());saveEditedEvent(index,e);});
  }else if(type.equals("music")||type.equals("sound")){
   assetDialogMatching("Substituir arquivo de áudio",new String[]{".ogg",".mp3",".wav"},file->{put(e,"file",file);saveEditedEvent(index,e);});
  }else if(type.equals("transition")){
   String[] effects={"dissolve","fade","pixellate","move"};new AlertDialog.Builder(this).setTitle("Transição").setItems(effects,(d,w)->{put(e,"effect",effects[w]);saveEditedEvent(index,e);}).show();
  }else if(type.equals("pause")){
   LinearLayout f=form();EditText value=field("Duração em segundos");value.setText(e.optString("value","1.0"));f.addView(value);
   editFormDialog("Editar espera",f,()->{put(e,"value",value.getText().toString());saveEditedEvent(index,e);});
  }else if(type.equals("hide")||type.equals("label")||type.equals("jump")||type.equals("call")){
   LinearLayout f=form();EditText target=field(type.equals("hide")?"Nome do personagem": "Nome do rótulo");target.setText(type.equals("hide")?e.optString("name"):e.optString("target"));f.addView(target);
   editFormDialog("Editar "+eventLabel(e),f,()->{put(e,type.equals("hide")?"name":"target",target.getText().toString());saveEditedEvent(index,e);});
  }else{
   toast("Este bloco não possui parâmetros editáveis.");
  }
 }
 private void flowPicker(){
  String[] labels={"Criar rótulo (label)","Pular para rótulo (jump)","Chamar rótulo (call)","Retornar"};
  new AlertDialog.Builder(this).setTitle("Controle de fluxo").setItems(labels,(d,w)->{
   if(w==3)addSimple("return");else addFlowEvent(w==0?"label":w==1?"jump":"call");
  }).show();
 }
 private void addFlowEvent(String type){
  LinearLayout f=form();EditText name=field("Nome do rótulo (ex.: capitulo_2)");f.addView(name);
  formDialog(type.equals("label")?"Novo rótulo":type.equals("jump")?"Pular para rótulo":"Chamar rótulo",f,()->{
   JSONObject e=new JSONObject();put(e,"type",type);put(e,"target",name.getText().toString());addEvent(e);
  });
 }
 private String eventLabel(JSONObject e){
  switch(e.optString("type")){
   case"say":return"DIÁLOGO";case"background":return"FUNDO";case"sprite":return"PERSONAGEM";case"layered":return"PERSONAGEM 3 PARTES";
   case"if":return"SE / CONDIÇÃO";case"else":return"SENÃO";case"endif":return"FIM DA CONDIÇÃO";case"set":return"DEFINIR VARIÁVEL";
   case"choice":return"ESCOLHA";case"music":return"TOCAR MÚSICA";case"sound":return"EFEITO SONORO";case"stopmusic":return"PARAR MÚSICA";
   case"transition":return"TRANSIÇÃO";case"pause":return"ESPERAR";case"hide":return"OCULTAR";case"plugin":return e.optString("plugin_label","AÇÃO DE PLUGIN");case"label":return"RÓTULO";case"jump":return"PULAR PARA";case"call":return"CHAMAR";case"return":return"RETORNAR";case"end":return"FIM DA HISTÓRIA";default:return e.optString("type").toUpperCase(Locale.ROOT);
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
  if(t.equals("plugin"))return e.optString("plugin_id")+":"+e.optString("action_id")+" • "+e.optString("plugin_label");\n  if(t.equals("label")||t.equals("jump")||t.equals("call"))return e.optString("target");
  if(t.equals("transition"))return e.optString("effect");
  return e.optString("file",e.optString("value",""));
 }
 private void removeEvent(int i){JSONArray n=new JSONArray();for(int j=0;j<events.length();j++)if(j!=i)n.put(events.opt(j));events=n;saveProject();showScreen();}
 private void moveEvent(int i,int d){int j=i+d;if(j<0||j>=events.length())return;Object a=events.opt(i),b=events.opt(j);try{events.put(j,a);events.put(i,b);saveProject();showScreen();}catch(JSONException ex){Toast.makeText(this,"Não foi possível mover o evento: "+ex.getMessage(),Toast.LENGTH_LONG).show();}}
 private void addEvent(JSONObject e){events.put(e);saveProject();showScreen();}
 private void eventPicker(){
  ArrayList<String> names=new ArrayList<>(Arrays.asList("Diálogo","Alterar fundo","Personagem (PNG)","Personagem em 3 PNGs","Definir variável","Escolha do jogador","Tocar música","Tocar efeito sonoro","Parar música","Transição","Esperar","Ocultar personagem","Senão","Fim da condição","Finalizar história"));
  List<PluginManager.Action> pluginActions=pluginManager==null?Collections.emptyList():pluginManager.getActions();
  for(PluginManager.Action action:pluginActions)names.add("PLUGIN • "+action.label);
  String[] choices=names.toArray(new String[0]);
  new AlertDialog.Builder(this).setTitle("Adicionar ação").setItems(choices,(d,w)->{
   if(w>=15){
    PluginManager.Action action=pluginActions.get(w-15);
    JSONObject event=new JSONObject();
    put(event,"type","plugin");put(event,"plugin_id",action.pluginId);put(event,"action_id",action.id);
    put(event,"plugin_label",action.label);put(event,"code",action.renpyCode);addEvent(event);return;
   }
   switch(w){
    case 0:addDialogue();break;case 1:addBackground();break;case 2:addSprite();break;case 3:addLayered();break;
    case 4:addSet();break;case 5:addChoice();break;case 6:addAudio("music");break;case 7:addAudio("sound");break;
    case 8:addSimple("stopmusic");break;case 9:addTransition();break;case 10:addPause();break;case 11:addHide();break;
    case 12:addSimple("else");break;case 13:addSimple("endif");break;case 14:addSimple("end");break;
   }
  }).show();
 }

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
 private void addIf(){
  LinearLayout f=form();EditText variable=field("Variável (ex.: encontrou_carta)"),value=field("Valor esperado");
  String[] ops={"==","!=",">","<",">=","<="};Spinner op=new Spinner(this);
  ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,ops);adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);op.setAdapter(adapter);
  f.addView(variable);f.addView(text("Comparador",12,MUTED,true));f.addView(op);f.addView(value);
  formDialog("Condição • SE",f,()->{
   JSONObject e=new JSONObject();put(e,"type","if");put(e,"variable",variable.getText().toString());
   put(e,"operator",ops[op.getSelectedItemPosition()]);put(e,"value",value.getText().toString());addEvent(e);
  });
 }
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
 private void pickPlugin(){
  Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.setType("application/zip");intent.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(intent,PICK_PLUGIN);
 }
 private void pluginScreen(){
  heading("Plugins / Mods","Adicione ações e extensões ao editor usando Lua ou bibliotecas C/C++ compiladas para Android.");
  content.addView(button("＋ Instalar plugin (.zip)",this::pickPlugin,true),lp(-1,48));space(12);
  LinearLayout warning=column();warning.addView(text("SEGURANÇA",11,0xffffcc80,true));
  warning.addView(text("Scripts Lua rodam com APIs limitadas. Bibliotecas .so são código nativo com permissões do aplicativo e podem alterar ou comprometer todo o app. Instale apenas plugins confiáveis.",12,TEXT,false));card(warning);
  content.addView(text("INSTALADOS",11,MINT,true));space(6);
  List<PluginManager.Plugin> installed=pluginManager==null?Collections.emptyList():pluginManager.getPlugins();
  if(installed.isEmpty())content.addView(text("Nenhum plugin instalado. Importe um ZIP com plugin.json.",13,MUTED,false));
  for(PluginManager.Plugin plugin:installed){
   LinearLayout item=column(),top=row();
   top.addView(text(plugin.name+"  •  v"+plugin.version,14,TEXT,true),new LinearLayout.LayoutParams(0,dp(34),1));
   Switch enabled=new Switch(this);enabled.setChecked(plugin.enabled);enabled.setOnCheckedChangeListener((b,v)->pluginManager.setEnabled(plugin.id,v));top.addView(enabled);item.addView(top);
   item.addView(text(plugin.id+"  •  "+plugin.type.toUpperCase(Locale.ROOT),11,MUTED,false));
   if(!plugin.error.isEmpty())item.addView(text("Erro: "+plugin.error,11,0xffff9a9a,false));
   TextView remove=text("Remover plugin",11,0xffff9a9a,true);remove.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Remover "+plugin.name+"?").setMessage("Os arquivos do plugin serão apagados.").setNegativeButton("Cancelar",null).setPositiveButton("Remover",(d,w)->pluginManager.remove(plugin.id)).show());item.addView(remove);card(item);
  }
  content.addView(text("AÇÕES REGISTRADAS",11,MINT,true));space(6);
  List<PluginManager.Action> actions=pluginManager==null?Collections.emptyList():pluginManager.getActions();
  if(actions.isEmpty())content.addView(text("Plugins ativos podem registrar blocos no botão Ação da folha de eventos.",12,MUTED,false));
  for(PluginManager.Action action:actions)content.addView(text(action.label+"  •  "+action.pluginId,12,TEXT,false),lp(-1,30));
 }
 private void assetScreen(){heading("Biblioteca de assets","Imagens PNG/JPG, músicas e efeitos sonoros.");content.addView(button("＋ Importar arquivo do dispositivo",this::pickAsset,true),lp(-1,48));space(14);if(assets.isEmpty())content.addView(text("Sua biblioteca está vazia. Importe sprites, fundos e áudio.",13,MUTED,false));for(String a:new ArrayList<>(assets)){LinearLayout r=row();r.addView(text("▧   "+a,13,TEXT,true),new LinearLayout.LayoutParams(0,dp(44),1));TextView del=text("Remover",11,0xffff8c9b,true);del.setOnClickListener(v->{assets.remove(a);new File(getFilesDir(),"assets/"+a).delete();saveProject();showScreen();});r.addView(del);card(r);}}
 private void pickAsset(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_ASSET);}
 @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(c!=RESULT_OK||d==null||d.getData()==null)return;if(r==PICK_ASSET)importAsset(d.getData());else if(r==EXPORT_ZIP)exportZip(d.getData());else if(r==PICK_PLUGIN){try{pluginManager.installZip(d.getData());showScreen();toast("Plugin instalado.");}catch(Exception ex){toast("Falha ao instalar plugin: "+ex.getMessage());}}}
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
    if(type.equals("background")){playfield.removeAllViews();playfield.setBackgroundColor(0xff28243a);characters[0]=new FrameLayout(this);playfield.addView(characters[0],new FrameLayout.LayoutParams(-1,-1));addImage(playfield,ev.optString("file"),floatValue(ev,"x",0.5f),floatValue(ev,"y",0.5f),floatValue(ev,"zoom",1),floatValue(ev,"rotation",0));playfield.bringChildToFront(characters[0]);}
    else if(type.equals("sprite")){characters[0].removeAllViews();addImage(characters[0],ev.optString("file"),floatValue(ev,"x",0.5f),floatValue(ev,"y",0.62f),floatValue(ev,"zoom",0.9f),floatValue(ev,"rotation",0));}
    else if(type.equals("layered")){characters[0].removeAllViews();float x=floatValue(ev,"x",0.5f),y=floatValue(ev,"y",0.62f),z=floatValue(ev,"zoom",0.9f),r=floatValue(ev,"rotation",0);addImage(characters[0],ev.optString("left"),x,y,z,r);addImage(characters[0],ev.optString("right"),x,y,z,r);addImage(characters[0],ev.optString("head"),x,y,z,r);}
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
 private void appearanceScreen(){
  heading("Aparência do jogo","Personalize a tela de título, botões, caixa de diálogo, cores e fontes.");
  content.addView(button("Imagem de fundo da tela de título",()->assetDialogMatching("Fundo da tela de título",new String[]{".png",".jpg",".jpeg",".webp"},v->setGui("title_bg",v)),true),lp(-1,46));space(7);
  content.addView(button("Imagem da caixa de diálogo",()->assetDialogMatching("Fundo da caixa de diálogo",new String[]{".png",".jpg",".jpeg",".webp"},v->setGui("dialogue_bg",v)),false),lp(-1,46));space(7);
  content.addView(button("Imagem dos botões (normal)",()->assetDialogMatching("Fundo normal do botão",new String[]{".png",".jpg",".jpeg",".webp"},v->setGui("button_idle_bg",v)),false),lp(-1,46));space(7);
  content.addView(button("Imagem dos botões (pressionado)",()->assetDialogMatching("Fundo ativo do botão",new String[]{".png",".jpg",".jpeg",".webp"},v->setGui("button_hover_bg",v)),false),lp(-1,46));space(7);
  content.addView(button("Fonte personalizada (.ttf / .otf)",()->assetDialogMatching("Escolher fonte",new String[]{".ttf",".otf"},v->setGui("font",v)),false),lp(-1,46));space(12);
  LinearLayout current=column();current.addView(text("RECURSOS SELECIONADOS",11,MINT,true));
  current.addView(text("Tela de título: "+guiSettings.optString("title_bg","padrão"),12,TEXT,false));
  current.addView(text("Caixa: "+guiSettings.optString("dialogue_bg","padrão"),12,TEXT,false));
  current.addView(text("Fonte: "+guiSettings.optString("font","padrão"),12,TEXT,false));card(current);
  content.addView(button("Cor do texto",()->editGuiColor("text_color","Cor do texto","#FFFFFF"),false),lp(-1,42));space(5);
  content.addView(button("Cor de destaque",()->editGuiColor("accent_color","Cor de destaque","#C8A2FF"),false),lp(-1,42));space(5);
  content.addView(button("Cor normal dos botões",()->editGuiColor("idle_color","Cor normal dos botões","#8A8299"),false),lp(-1,42));space(5);
  content.addView(button("Cor ao tocar nos botões",()->editGuiColor("hover_color","Cor ao tocar nos botões","#FFFFFF"),false),lp(-1,42));space(5);
  content.addView(button("Tamanho do texto",()->editGuiNumber("text_size","Tamanho do texto",16,48,30),false),lp(-1,42));space(5);
  content.addView(button("Altura da caixa de diálogo",()->editGuiNumber("textbox_height","Altura da caixa",120,420,230),false),lp(-1,42));space(10);
  content.addView(text("As imagens e fontes selecionadas serão copiadas para o ZIP. O Ren'Py usa os arquivos exportados por caminhos relativos à pasta game/.",11,MUTED,false));
 }
 private void setGui(String key,String value){put(guiSettings,key,value);saveProject();showScreen();}
 private void editGuiColor(String key,String title,String fallback){
  LinearLayout f=form();EditText e=field("Cor hexadecimal, ex.: #AABBCC");e.setText(guiSettings.optString(key,fallback));f.addView(e);
  editFormDialog(title,f,()->{String v=e.getText().toString().trim();if(!v.matches("#[0-9a-fA-F]{6}")){toast("Informe uma cor no formato #RRGGBB.");return;}setGui(key,v);});
 }
 private void editGuiNumber(String key,String title,int min,int max,int fallback){
  LinearLayout f=form();int value=guiSettings.optInt(key,fallback);SeekBar seek=addSlider(f,title,max-min,Math.max(0,Math.min(max-min,value-min)));
  editFormDialog(title,f,()->setGui(key,Integer.toString(seek.getProgress()+min)));
 }
 private String pyQuote(String value){
  return "\"" + value.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","") + "\"";
 }
 private String buildGuiScript(){
  StringBuilder out=new StringBuilder("# Generated GUI settings.\ninit 999 python:\n");
  String font=guiSettings.optString("font","");if(!font.isEmpty()){
   out.append("    gui.text_font = ").append(pyQuote("fonts/"+font)).append("\n");
   out.append("    gui.interface_text_font = ").append(pyQuote("fonts/"+font)).append("\n");
   out.append("    gui.button_text_font = ").append(pyQuote("fonts/"+font)).append("\n");
  }
  out.append("    gui.text_color = ").append(pyQuote(guiSettings.optString("text_color","#FFFFFF"))).append("\n");
  out.append("    gui.accent_color = ").append(pyQuote(guiSettings.optString("accent_color","#C8A2FF"))).append("\n");
  out.append("    gui.idle_color = ").append(pyQuote(guiSettings.optString("idle_color","#8A8299"))).append("\n");
  out.append("    gui.hover_color = ").append(pyQuote(guiSettings.optString("hover_color","#FFFFFF"))).append("\n");
  out.append("    gui.button_text_idle_color = gui.idle_color\n");
  out.append("    gui.button_text_hover_color = gui.hover_color\n");
  out.append("    gui.choice_button_text_idle_color = gui.idle_color\n");
  out.append("    gui.choice_button_text_hover_color = gui.hover_color\n");
  out.append("    gui.text_size = ").append(guiSettings.optInt("text_size",30)).append("\n");
  out.append("    gui.name_text_size = ").append(guiSettings.optInt("text_size",30)+6).append("\n");
  out.append("    gui.textbox_height = ").append(guiSettings.optInt("textbox_height",230)).append("\n");
  return out.toString();
 }
 
 private String buildRpaScript(){
  return "# Ren'Py archive configuration. Run Build Distributions in the Ren'Py Launcher to create .rpa files.\n"+
   "init python:\n"+
   "    build.archive('studio_gui', 'all')\n"+
   "    build.classify('game/gui/**', 'studio_gui')\n"+
   "    build.archive('studio_images', 'all')\n"+
   "    build.classify('game/images/**', 'studio_images')\n"+
   "    build.archive('studio_audio', 'all')\n"+
   "    build.classify('game/audio/**', 'studio_audio')\n"+
   "    build.archive('studio_fonts', 'all')\n"+
   "    build.classify('game/fonts/**', 'studio_fonts')\n";
 }
 
 private String buildRpycShell(){
  return "#!/bin/sh\nset -eu\nif [ \"$#\" -ne 1 ]; then echo \"Usage: RENPY_SDK=/path/to/renpy-sdk $0 /path/to/project\"; exit 2; fi\n"+
   "if [ -z \"$RENPY_SDK\" ]; then echo \"Set RENPY_SDK to your Ren'Py SDK directory\"; exit 2; fi\n"+
   "\"$RENPY_SDK/renpy.sh\" \"$1\" compile\n";
 }
 private String buildRpycBat(){
  return "@echo off\nif \"%~1\"==\"\" (echo Usage: set RENPY_SDK=C:\\RenPy\\sdk then COMPILE_RPYC.bat C:\\MyGame & exit /b 2)\n"+
   "if \"%RENPY_SDK%\"==\"\" (echo Set RENPY_SDK to your Ren'Py SDK folder. & exit /b 2)\n"+
   "\"%RENPY_SDK%\\lib\\py3-windows-x86_64\\python.exe\" \"%RENPY_SDK%\\renpy.py\" \"%~1\" compile\n";
 }
 private void exportScreen(){
  heading("Exportar para Ren'Py","Gere um projeto pronto para extrair na pasta game/.");
  LinearLayout info=column();info.addView(text("PACOTE DE EXPORTAÇÃO",11,MINT,true));spaceIn(info,8);
  info.addView(text("• script.rpy gerado em C++\n• imagens, áudio e fontes personalizadas\n• configuração de GUI/tela de título\n• configuração opcional de arquivos .rpa\n• scripts opcionais para compilar .rpy em .rpyc",14,TEXT,false));card(info);
  CheckBox rpyc=new CheckBox(this);rpyc.setText("Incluir scripts para compilar .rpy → .rpyc com o SDK Ren'Py");rpyc.setTextColor(TEXT);rpyc.setChecked(includeRpycHelper);rpyc.setOnCheckedChangeListener((b,v)->{includeRpycHelper=v;saveProject();});content.addView(rpyc);
  CheckBox rpa=new CheckBox(this);rpa.setText("Incluir configuração para gerar arquivos .rpa");rpa.setTextColor(TEXT);rpa.setChecked(includeRpaConfig);rpa.setOnCheckedChangeListener((b,v)->{includeRpaConfig=v;saveProject();});content.addView(rpa);
  content.addView(text("O app inclui a configuração e os comandos oficiais no pacote. A compilação .rpyc e a criação real dos .rpa são executadas pelo SDK/Launcher do Ren'Py.",11,MUTED,false));space(12);
  content.addView(button("⇩  Gerar ZIP do projeto",()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/zip");i.putExtra(Intent.EXTRA_TITLE,projectId+"_renpy.zip");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,EXPORT_ZIP);},true),lp(-1,54));
 }
 private void copyAssetToZip(ZipOutputStream z,String asset,String destination)throws Exception{
  File file=new File(getFilesDir(),"assets/"+asset);if(!file.isFile())throw new IOException("Asset não encontrado: "+asset);
  try(InputStream in=new FileInputStream(file)){
   z.putNextEntry(new ZipEntry(destination));byte[] buffer=new byte[8192];int count;
   while((count=in.read(buffer))>0)z.write(buffer,0,count);
   z.closeEntry();
  }
 }
 private void exportZip(Uri target){
  try{
   StringBuilder input=new StringBuilder();
   for(int i=0;i<events.length();i++){
    JSONObject e=events.optJSONObject(i);if(e==null)continue;String t=e.optString("type");ArrayList<String> v=new ArrayList<>();
    switch(t){
     case"say":v.add(e.optString("speaker"));v.add(e.optString("text"));break;
     case"background":v.add(e.optString("file"));v.add(e.optString("x","0.5"));v.add(e.optString("y","0.5"));v.add(e.optString("zoom","1.0"));v.add(e.optString("rotation","0"));break;
     case"sprite":v.add(e.optString("name"));v.add(e.optString("file"));v.add(e.optString("x","0.5"));v.add(e.optString("y","0.62"));v.add(e.optString("zoom","0.9"));v.add(e.optString("rotation","0"));break;
     case"layered":v.add(e.optString("name"));v.add(e.optString("left"));v.add(e.optString("right"));v.add(e.optString("head"));v.add(e.optString("x","0.5"));v.add(e.optString("y","0.62"));v.add(e.optString("zoom","0.9"));v.add(e.optString("rotation","0"));break;
     case"if":v.add(e.optString("variable"));v.add(e.optString("operator","=="));v.add(e.optString("value"));break;
     case"set":v.add(e.optString("variable"));v.add(e.optString("value"));break;
     case"choice":v.add(e.optString("a"));v.add(e.optString("b"));break;
     case"music":case"sound":v.add(e.optString("file"));break;
     case"transition":v.add(e.optString("effect"));break;
     case"pause":v.add(e.optString("value"));break;
     case"hide":v.add(e.optString("name"));break;
     case"label":case"jump":case"call":v.add(e.optString("target"));break;\n     case"plugin":v.add(e.optString("plugin_id"));v.add(e.optString("action_id"));v.add(e.optString("code"));break;
    }
    input.append(t);
    for(String value:v)input.append("|").append(Base64.getEncoder().encodeToString(value.getBytes("UTF-8")));
    input.append("\n");
   }
   String script=NativeBridge.compileStory(input.toString());
   OutputStream rawOut=getContentResolver().openOutputStream(target);if(rawOut==null)throw new IOException("Não foi possível criar o ZIP.");
   try(ZipOutputStream z=new ZipOutputStream(rawOut)){
    putZip(z,"script.rpy",script);
    putZip(z,"zz_studio_gui.rpy",buildGuiScript());
    if(includeRpaConfig)putZip(z,"zz_studio_archives.rpy",buildRpaScript());
    if(includeRpycHelper){putZip(z,"COMPILE_RPYC.sh",buildRpycShell());putZip(z,"COMPILE_RPYC.bat",buildRpycBat());}
    putZip(z,"README_RENPY_STUDIO.txt","Ren'Py Studio Mobile export\nExtract these files into an existing Ren'Py project's game/ directory.\nThe title screen image is gui/main_menu.png; dialogue box is gui/textbox.png.\nButton backgrounds are in gui/button/; fonts are in fonts/.\nUse Build Distributions in the Ren'Py Launcher to create .rpa archives.\nRun COMPILE_RPYC.sh or COMPILE_RPYC.bat with a Ren'Py SDK to compile .rpy into .rpyc.\n");
    HashSet<String> guiAssets=new HashSet<>();
    String title=guiSettings.optString("title_bg",""),dialogue=guiSettings.optString("dialogue_bg","");
    String idle=guiSettings.optString("button_idle_bg",""),hover=guiSettings.optString("button_hover_bg","");
    if(!title.isEmpty()){copyAssetToZip(z,title,"gui/main_menu.png");guiAssets.add(title);}
    if(!dialogue.isEmpty()){copyAssetToZip(z,dialogue,"gui/textbox.png");guiAssets.add(dialogue);}
    if(!idle.isEmpty()){copyAssetToZip(z,idle,"gui/button/idle_background.png");guiAssets.add(idle);}
    if(!hover.isEmpty()){copyAssetToZip(z,hover,"gui/button/hover_background.png");guiAssets.add(hover);}
    for(String asset:assets){
     if(guiAssets.contains(asset))continue;
     File file=new File(getFilesDir(),"assets/"+asset);if(!file.isFile())continue;
     String lower=asset.toLowerCase(Locale.ROOT);
     String folder=(lower.endsWith(".ogg")||lower.endsWith(".mp3")||lower.endsWith(".wav"))?"audio/":
       (lower.endsWith(".ttf")||lower.endsWith(".otf"))?"fonts/":"images/";
     copyAssetToZip(z,asset,folder+asset);
    }
   }
   toast("ZIP exportado.");
  }catch(Exception e){toast("Erro na exportação: "+e.getMessage());}
 }
 private void putZip(ZipOutputStream z,String n,String s)throws Exception{z.putNextEntry(new ZipEntry(n));z.write(s.getBytes("UTF-8"));z.closeEntry();}
 private void renameProject(){LinearLayout f=form();EditText e=field("Nome do projeto");e.setText(projectName);f.addView(e);formDialog("Nome do projeto",f,()->{projectName=e.getText().toString().trim();if(projectName.isEmpty())projectName="Minha Visual Novel";projectId=projectName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","_");saveProject();showScreen();});}
 private void put(JSONObject o,String k,String v){try{o.put(k,v);}catch(Exception ignored){}}
 private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
