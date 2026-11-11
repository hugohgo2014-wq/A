# Assistant Revival

Projeto Android ARM64 (arm64-v8a) com interface Java, renderização alternativa OpenGL ES 3.0 em C++, e inferência local via llama.cpp.

## Estado real desta entrega

Este é um projeto-fonte inicial, não um APK previamente testado em dispositivo. O workflow do GitHub Actions tenta compilar e publicar um APK de depuração. O código-fonte do llama.cpp não é copiado para este ZIP: o CMake usa `FetchContent` para baixar e compilar automaticamente a versão upstream `v0.7.0` durante o GitHub Actions. O código JNI chama a API C de `llama.h` para carregar o GGUF e gerar respostas. A versão está fixada em `v0.7.0` para evitar mudanças inesperadas da branch master. Ainda é necessário validar o build Android real no GitHub Actions. Não há mecanismo de atualização automática do aplicativo.

## Onde está o llama.cpp?

A integração está em `app/src/main/cpp/CMakeLists.txt`. Na etapa `externalNativeBuild` do Gradle, o CMake baixa `https://github.com/ggml-org/llama.cpp` na tag `v0.7.0`, compila a biblioteca nativa e a liga ao `assistant_native`. O arquivo `native-lib.cpp` inclui `llama.h` e usa `llama_model_load_from_file`, `llama_init_from_model`, tokenização, decode e sampler. Portanto, o llama.cpp não fica visível como pasta dentro do ZIP: é obtido automaticamente durante a compilação. É necessária internet no runner do GitHub Actions.

## Modelo

Modelo esperado: `gemma-3-1b-it-Q4_K_M.gguf` (GGUF). Coloque o arquivo na pasta `models/` para mantê-lo junto do projeto, mas o APK não empacota o modelo. No primeiro uso, abra Configurações > Importar modelo GGUF e selecione o arquivo. O app copia o modelo para o diretório privado.

O modelo deve ser obtido de uma fonte que distribua especificamente Gemma 3 1B IT em GGUF. Verifique a licença Gemma e os termos do repositório de pesos antes de redistribuir.

## Compilar no GitHub

1. Crie um repositório GitHub e envie todo o conteúdo desta pasta. Se for incluir o modelo, configure Git LFS e envie `models/gemma-3-1b-it-Q4_K_M.gguf`.
2. Abra Actions.
3. Execute `Build Assistant Revival APK` (ou faça push para `main`).
4. Baixe o artefato `assistant-revival-debug-arm64`.

O workflow usa JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3, Android NDK 27.2.12479018 e CMake 3.22.1.

## Compilar localmente

Com Android SDK, NDK e CMake instalados:

```bash
gradle --no-daemon :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Renderizadores

- Java: tela e controles Android nativos.
- OpenGL: `GLSurfaceView` cria o contexto ES 3.0; os shaders e chamadas de desenho são executados em C++ (`gl_renderer.cpp`). Java continua responsável pelo ciclo de vida da Activity e pelos controles do sistema operacional.
- A seleção fica em Configurações e é aplicada ao abrir a tela novamente.

## Recursos incluídos

- Chat local com o modelo carregado via JNI.
- Importação do GGUF pelo seletor de documentos Android.
- Entrada de voz com `SpeechRecognizer` e resposta falada com `TextToSpeech`.
- Comandos básicos: hora, abrir aplicativo pelo nome, volume e abrir tela de criação de alarme.
- Renderizador OpenGL ES 3.0 alternativo.

## Limitações conhecidas

- O reconhecimento de voz do Android pode usar o serviço de reconhecimento instalado no aparelho e não é garantidamente offline. A geração do Gemma é local depois de importar o modelo.
- O comando de lanterna ainda não está implementado; o app orienta usar os controles rápidos.
- Não há palavra de ativação contínua (`Ok Google`), serviço de escuta permanente, controle irrestrito do sistema, nem substituição completa das integrações proprietárias do Google Assistente.
- O suporte a comandos será ampliado; a IA não recebe execução arbitrária de código.
- O Gemma 3 1B pode consumir bastante RAM. Em aparelhos com pouca memória, pode encerrar por falta de memória ou responder lentamente. O contexto inicial é limitado a 2048 tokens.
- O projeto está configurado somente para `arm64-v8a`.

## Dependências e referências

- llama.cpp: https://github.com/ggml-org/llama.cpp
- Guia Android llama.cpp: https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md
- Android NDK: https://developer.android.com/ndk
- Android SpeechRecognizer: https://developer.android.com/reference/android/speech/SpeechRecognizer
