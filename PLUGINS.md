# Plugins e mods — Ren'Py Studio Mobile

O Ren'Py Studio Mobile aceita plugins compactados em ZIP. O ZIP precisa conter plugin.json na raiz. Plugins podem adicionar blocos à folha de eventos.

## Lua

Estrutura:

```text
meu-plugin.zip
├── plugin.json
└── main.lua
```

plugin.json:

```json
{
  "id": "exemplo.bloco_extra",
  "name": "Bloco extra de exemplo",
  "version": "1.0.0",
  "type": "lua",
  "entry": "main.lua"
}
```

main.lua:

```lua
register_action(
    "narracao_em_itálico",
    "Narrar em itálico",
    "{i}Texto narrado{/i}"
)
```

A função register_action(id, label, renpy_code) registra uma ação que aparece em Studio → Eventos → Ação. O terceiro argumento é código Ren'Py que será inserido no ponto do evento.

O interpretador Lua é LuaJ 3.0.1 (licença MIT). O ambiente do plugin inclui funções básicas, strings, tabelas e matemática. Não são disponibilizados io, os, package, require ou luajava. Isso reduz o acesso direto ao dispositivo; não é uma garantia contra loops infinitos, consumo excessivo de CPU/memória ou código malicioso. Instale apenas plugins de fontes confiáveis.

## C ou C++ nativo

Plugins nativos são bibliotecas Android .so. Compile uma biblioteca para uma das ABIs incluídas no APK: arm64-v8a ou armeabi-v7a.

Estrutura:

```text
meu-plugin.zip
├── plugin.json
└── lib/
    ├── arm64-v8a/
    │   └── libmeu_plugin.so
    └── armeabi-v7a/
        └── libmeu_plugin.so
```

plugin.json:

```json
{
  "id": "exemplo.modulo_nativo",
  "name": "Módulo nativo",
  "version": "1.0.0",
  "type": "native",
  "entry": "libmeu_plugin.so"
}
```

O módulo chama a API JNI Java durante JNI_OnLoad para registrar ações:

```cpp
#include <jni.h>

extern "C" JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void*) {
    JNIEnv* env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass host = env->FindClass("studio/renpy/mobile/PluginManager");
    if (!host) return JNI_ERR;

    jmethodID registerAction = env->GetStaticMethodID(
        host,
        "registerNativeAction",
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"
    );
    if (!registerAction) return JNI_ERR;

    jstring id = env->NewStringUTF("mostrar_aviso");
    jstring label = env->NewStringUTF("Mostrar aviso");
    jstring code = env->NewStringUTF("centered \"Plugin nativo carregado\"");

    env->CallStaticVoidMethod(host, registerAction, id, label, code);
    env->DeleteLocalRef(id);
    env->DeleteLocalRef(label);
    env->DeleteLocalRef(code);
    env->DeleteLocalRef(host);

    return env->ExceptionCheck() ? JNI_ERR : JNI_VERSION_1_6;
}
```

**Aviso:** bibliotecas nativas rodam dentro do processo do app e podem ler/escrever dados acessíveis ao aplicativo, travar o processo ou executar qualquer operação permitida pelo Android. O sistema não consegue isolá-las como um processo separado. A instalação é uma decisão de confiança do usuário.

## Limites atuais da API

- A API Lua registra ações de eventos com código Ren'Py.
- A API nativa pode usar JNI e registrar ações por PluginManager.registerNativeAction.
- Os blocos registrados são exportados junto com script.rpy.
- Plugins não são convertidos automaticamente entre Lua, C e C++; cada módulo deve ser compilado/empacotado para o runtime Android.
- Desativar um plugin impede que ele seja carregado no próximo reload. Uma biblioteca .so já carregada pelo Android não pode ser descarregada com segurança; reinicie o app após desativar ou remover plugins nativos.
