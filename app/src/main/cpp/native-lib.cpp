#include <jni.h>
#include <string>
#include <mutex>
#include <vector>
#include <cstring>
#include <android/log.h>
#include "llama.h"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "AssistantRevival", __VA_ARGS__)
static llama_model *g_model = nullptr;
static llama_context *g_ctx = nullptr;
static std::mutex g_mutex;
static std::once_flag g_backend_once;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_revival_assistant_NativeBridge_loadModel(JNIEnv *env, jclass, jstring path_) {
    const char *path = env->GetStringUTFChars(path_, nullptr);
    std::lock_guard<std::mutex> lock(g_mutex);
    if (g_ctx) { llama_free(g_ctx); g_ctx = nullptr; }
    if (g_model) { llama_model_free(g_model); g_model = nullptr; }
    std::call_once(g_backend_once, [](){ llama_backend_init(); });
    llama_model_params mp = llama_model_default_params();
    mp.n_gpu_layers = 0;
    g_model = llama_model_load_from_file(path, mp);
    env->ReleaseStringUTFChars(path_, path);
    if (!g_model) { LOGE("Model load failed"); return JNI_FALSE; }
    llama_context_params cp = llama_context_default_params();
    cp.n_ctx = 2048;
    cp.n_batch = 256;
    cp.n_threads = 4;
    g_ctx = llama_init_from_model(g_model, cp);
    if (!g_ctx) { llama_model_free(g_model); g_model = nullptr; return JNI_FALSE; }
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_revival_assistant_NativeBridge_generate(JNIEnv *env, jclass, jstring prompt_) {
    const char *prompt = env->GetStringUTFChars(prompt_, nullptr);
    std::lock_guard<std::mutex> lock(g_mutex);
    if (!g_ctx || !g_model) { env->ReleaseStringUTFChars(prompt_, prompt); return env->NewStringUTF("O modelo ainda não foi carregado. Importe o arquivo GGUF nas configurações."); }
    const llama_vocab *vocab = llama_model_get_vocab(g_model);
    std::vector<llama_token> tokens(strlen(prompt) + 32);
    int n = llama_tokenize(vocab, prompt, (int)strlen(prompt), tokens.data(), (int)tokens.size(), true, true);
    if (n < 0) { tokens.resize((size_t)(-n)); n = llama_tokenize(vocab, prompt, (int)strlen(prompt), tokens.data(), (int)tokens.size(), true, true); }
    if (n <= 0) { env->ReleaseStringUTFChars(prompt_, prompt); return env->NewStringUTF("Não consegui interpretar a solicitação."); }
    tokens.resize(n);
    llama_memory_clear(llama_get_memory(g_ctx), true);
    llama_batch batch = llama_batch_get_one(tokens.data(), (int)tokens.size());
    if (llama_decode(g_ctx, batch) != 0) { env->ReleaseStringUTFChars(prompt_, prompt); return env->NewStringUTF("Falha durante a inferência."); }
    llama_sampler *sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(sampler, llama_sampler_init_top_p(0.90f, 1));
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(0.7f));
    llama_sampler_chain_add(sampler, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));
    std::string output;
    for (int i = 0; i < 160; ++i) {
        llama_token id = llama_sampler_sample(sampler, g_ctx, -1);
        if (llama_vocab_is_eog(vocab, id)) break;
        char piece[256];
        int len = llama_token_to_piece(vocab, id, piece, sizeof(piece), 0, true);
        if (len > 0) output.append(piece, len);
        batch = llama_batch_get_one(&id, 1);
        if (llama_decode(g_ctx, batch) != 0) break;
    }
    llama_sampler_free(sampler);
    env->ReleaseStringUTFChars(prompt_, prompt);
    return env->NewStringUTF(output.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_revival_assistant_NativeBridge_isLoaded(JNIEnv *, jclass) {
    std::lock_guard<std::mutex> lock(g_mutex);
    return (g_model && g_ctx) ? JNI_TRUE : JNI_FALSE;
}
