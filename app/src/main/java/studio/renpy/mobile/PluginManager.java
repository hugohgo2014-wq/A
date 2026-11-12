package studio.renpy.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

import org.json.JSONObject;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.compiler.LuaC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class PluginManager {
    public interface Listener {
        void onPluginStateChanged();
    }

    public static final class Plugin {
        public final String id;
        public final String name;
        public final String version;
        public final String type;
        public final boolean enabled;
        public final String error;

        Plugin(String id, String name, String version, String type, boolean enabled, String error) {
            this.id = id;
            this.name = name;
            this.version = version;
            this.type = type;
            this.enabled = enabled;
            this.error = error;
        }
    }

    public static final class Action {
        public final String pluginId;
        public final String id;
        public final String label;
        public final String renpyCode;

        Action(String pluginId, String id, String label, String renpyCode) {
            this.pluginId = pluginId;
            this.id = id;
            this.label = label;
            this.renpyCode = renpyCode;
        }
    }

    private static volatile PluginManager activeManager;
    private static volatile String loadingNativePlugin = "";

    private final Context context;
    private final SharedPreferences preferences;
    private final File pluginRoot;
    private final Listener listener;
    private final ArrayList<Plugin> plugins = new ArrayList<>();
    private final ArrayList<Action> actions = new ArrayList<>();

    public PluginManager(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences("renpy_plugins", Context.MODE_PRIVATE);
        this.pluginRoot = new File(context.getFilesDir(), "plugins");
        this.listener = listener;
        if (!pluginRoot.exists()) pluginRoot.mkdirs();
        activeManager = this;
        reload();
    }

    public synchronized List<Plugin> getPlugins() {
        return new ArrayList<>(plugins);
    }

    public synchronized List<Action> getActions() {
        return new ArrayList<>(actions);
    }

    public synchronized void installZip(Uri uri) throws Exception {
        File staging = new File(pluginRoot, ".install_" + System.nanoTime());
        deleteTree(staging);
        if (!staging.mkdirs()) throw new Exception("Não foi possível criar a pasta temporária.");

        long totalBytes = 0;
        try {
            InputStream raw = context.getContentResolver().openInputStream(uri);
            if (raw == null) throw new Exception("Arquivo de plugin inacessível.");
            try (InputStream input = raw; ZipInputStream zip = new ZipInputStream(input)) {
                ZipEntry entry;
                byte[] buffer = new byte[8192];
                while ((entry = zip.getNextEntry()) != null) {
                    String name = entry.getName().replace('\\', '/');
                    if (name.isEmpty() || name.startsWith("/") || name.matches("^[A-Za-z]:.*")
                            || name.equals("..") || name.contains("../") || name.contains("/..")) {
                        throw new Exception("O ZIP contém um caminho inválido.");
                    }

                    File output = new File(staging, name);
                    String rootPath = staging.getCanonicalPath() + File.separator;
                    String outputPath = output.getCanonicalPath();
                    if (!outputPath.startsWith(rootPath) && !outputPath.equals(staging.getCanonicalPath())) {
                        throw new Exception("O ZIP tentou sair da pasta de instalação.");
                    }

                    if (entry.isDirectory()) {
                        if (!output.exists() && !output.mkdirs()) throw new Exception("Falha ao criar diretório.");
                    } else {
                        File parent = output.getParentFile();
                        if (parent != null && !parent.exists() && !parent.mkdirs()) {
                            throw new Exception("Falha ao criar diretório do plugin.");
                        }
                        try (FileOutputStream out = new FileOutputStream(output)) {
                            int count;
                            while ((count = zip.read(buffer)) != -1) {
                                if (count == 0) continue;
                                totalBytes += count;
                                if (totalBytes > 32L * 1024L * 1024L) {
                                    throw new Exception("O plugin excede 32 MB descompactados.");
                                }
                                out.write(buffer, 0, count);
                            }
                        }
                    }
                    zip.closeEntry();
                }
            }

            File manifestFile = new File(staging, "plugin.json");
            if (!manifestFile.isFile()) throw new Exception("O ZIP precisa conter plugin.json na raiz.");

            JSONObject manifest = new JSONObject(readText(manifestFile, 256 * 1024));
            String id = manifest.optString("id", "").toLowerCase(Locale.ROOT);
            String type = manifest.optString("type", "").toLowerCase(Locale.ROOT);
            String entry = manifest.optString("entry", type.equals("lua") ? "main.lua" : "");
            if (!id.matches("[a-z0-9_.-]{1,64}")) throw new Exception("ID inválido em plugin.json.");
            if (!type.equals("lua") && !type.equals("native")) {
                throw new Exception("O campo type deve ser 'lua' ou 'native'.");
            }
            if (entry.isEmpty() || entry.startsWith("/") || entry.contains("..")) {
                throw new Exception("O caminho entry do plugin é inválido.");
            }
            File entryFile = new File(staging, entry);
            if (!entryFile.getCanonicalPath().startsWith(staging.getCanonicalPath() + File.separator)
                    || !entryFile.isFile()) {
                throw new Exception("O arquivo entry do plugin não existe ou é inválido.");
            }
            if (type.equals("native") && !entry.toLowerCase(Locale.ROOT).endsWith(".so")) {
                throw new Exception("Plugin nativo precisa apontar para uma biblioteca .so.");
            }

            File destination = new File(pluginRoot, id);
            deleteTree(destination);
            if (!staging.renameTo(destination)) throw new Exception("Não foi possível concluir a instalação.");
            preferences.edit().putBoolean("enabled_" + id, true).apply();
            reload();
        } catch (Exception ex) {
            deleteTree(staging);
            throw ex;
        }
    }

    public synchronized void setEnabled(String id, boolean enabled) {
        preferences.edit().putBoolean("enabled_" + id, enabled).apply();
        reload();
    }

    public synchronized void remove(String id) {
        deleteTree(new File(pluginRoot, id));
        preferences.edit().remove("enabled_" + id).apply();
        reload();
    }

    public synchronized void reload() {
        plugins.clear();
        actions.clear();

        File[] folders = pluginRoot.listFiles(File::isDirectory);
        if (folders != null) {
            for (File folder : folders) {
                if (folder.getName().startsWith(".")) continue;
                String id = folder.getName();
                String name = id;
                String version = "1.0.0";
                String type = "lua";
                boolean enabled = preferences.getBoolean("enabled_" + id, true);
                String error = "";
                try {
                    File manifestFile = new File(folder, "plugin.json");
                    if (!manifestFile.isFile()) throw new Exception("plugin.json ausente.");
                    JSONObject manifest = new JSONObject(readText(manifestFile, 256 * 1024));
                    id = manifest.optString("id", id).toLowerCase(Locale.ROOT);
                    name = manifest.optString("name", id);
                    version = manifest.optString("version", "1.0.0");
                    type = manifest.optString("type", "lua").toLowerCase(Locale.ROOT);
                    if (!id.matches("[a-z0-9_.-]{1,64}")) throw new Exception("ID inválido.");
                    if (enabled) {
                        if (type.equals("lua")) loadLuaPlugin(folder, manifest, id);
                        else if (type.equals("native")) loadNativePlugin(folder, manifest, id);
                        else throw new Exception("Tipo de plugin não suportado.");
                    }
                } catch (Exception ex) {
                    error = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
                }
                plugins.add(new Plugin(id, name, version, type, enabled, error));
            }
        }

        if (listener != null) listener.onPluginStateChanged();
    }

    private void loadLuaPlugin(File folder, JSONObject manifest, String id) throws Exception {
        String entry = manifest.optString("entry", "main.lua");
        File script = new File(folder, entry);
        String rootPath = folder.getCanonicalPath() + File.separator;
        if (!script.getCanonicalPath().startsWith(rootPath) || !script.isFile()) {
            throw new Exception("Arquivo Lua principal não encontrado.");
        }

        final String source = readText(script, 2 * 1024 * 1024);
        FutureTask<Void> task = new FutureTask<>(() -> {
            Globals globals = new Globals();
            globals.load(new JseBaseLib());
            globals.load(new TableLib());
            globals.load(new StringLib());
            globals.load(new JseMathLib());
            LoadState.install(globals);
            LuaC.install(globals);

            // Deliberately omit filesystem, process, package and Java-bridge libraries.
            for (String blocked : new String[]{"io", "os", "debug", "package", "require",
                    "dofile", "loadfile", "load", "luajava", "collectgarbage"}) {
                globals.set(blocked, LuaValue.NIL);
            }

            globals.set("register_action", new VarArgFunction() {
                @Override public Varargs invoke(Varargs args) {
                    if (args.narg() < 3) return LuaValue.NIL;
                    registerAction(id, args.arg(1).tojstring(), args.arg(2).tojstring(),
                            args.arg(3).tojstring());
                    return LuaValue.NIL;
                }
            });
            globals.load(source, script.getName()).call();
            return null;
        });

        Thread thread = new Thread(task, "RenPyLuaPlugin-" + id);
        thread.setDaemon(true);
        thread.start();
        try {
            task.get(3, TimeUnit.SECONDS);
        } catch (Exception ex) {
            task.cancel(true);
            throw new Exception("Falha ou timeout ao carregar Lua: " + ex.getMessage());
        }
    }

    private void loadNativePlugin(File folder, JSONObject manifest, String id) throws Exception {
        String entry = manifest.optString("entry", "");
        String requested = new File(entry).getName();
        File selected = null;
        for (String abi : Build.SUPPORTED_ABIS) {
            File candidate = new File(folder, "lib/" + abi + "/" + requested);
            if (candidate.isFile()) {
                selected = candidate;
                break;
            }
        }
        if (selected == null) throw new Exception("Não existe .so para a ABI deste dispositivo.");

        File destination = new File(new File(context.getCodeCacheDir(), "plugins"), id + "_" + requested);
        File parent = destination.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new Exception("Não foi possível preparar o módulo nativo.");
        }
        copyFile(selected, destination);
        destination.setReadable(true, true);
        destination.setWritable(false, false);

        loadingNativePlugin = id;
        try {
            System.load(destination.getAbsolutePath());
        } finally {
            loadingNativePlugin = "";
        }
    }

    private void registerAction(String pluginId, String actionId, String label, String code) {
        if (actionId == null || !actionId.matches("[A-Za-z0-9_.-]{1,64}")) return;
        if (label == null || label.trim().isEmpty() || code == null || code.trim().isEmpty()) return;
        for (Action action : actions) {
            if (action.pluginId.equals(pluginId) && action.id.equals(actionId)) return;
        }
        actions.add(new Action(pluginId, actionId, label, code));
    }

    // Native modules may call this during JNI_OnLoad.
    public static void registerNativeAction(String actionId, String label, String renpyCode) {
        PluginManager manager = activeManager;
        String pluginId = loadingNativePlugin;
        if (manager != null && pluginId != null && !pluginId.isEmpty()) {
            manager.registerAction(pluginId, actionId, label, renpyCode);
        }
    }

    private static String readText(File file, int limit) throws Exception {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            char[] buffer = new char[4096];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                if (result.length() + count > limit) throw new Exception("Arquivo excede o limite permitido.");
                result.append(buffer, 0, count);
            }
        }
        return result.toString();
    }

    private static void copyFile(File source, File destination) throws Exception {
        try (InputStream in = new FileInputStream(source); FileOutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = in.read(buffer)) > 0) out.write(buffer, 0, count);
        }
    }

    private static void deleteTree(File file) {
        if (file == null || !file.exists()) return;
        File[] children = file.listFiles();
        if (children != null) for (File child : children) deleteTree(child);
        file.delete();
    }
}