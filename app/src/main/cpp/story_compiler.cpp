#include <jni.h>
#include <string>
#include <vector>
#include <sstream>
#include <set>
#include <map>
#include <algorithm>
#include <cctype>
#include <cstdlib>

static std::string b64(const std::string& in) {
    static const std::string chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    std::string out;
    int val = 0, valb = -8;
    for (unsigned char c : in) {
        if (c == '=') break;
        const auto p = chars.find(c);
        if (p == std::string::npos) continue;
        val = (val << 6) + static_cast<int>(p);
        valb += 6;
        if (valb >= 0) {
            out.push_back(static_cast<char>((val >> valb) & 255));
            valb -= 8;
        }
    }
    return out;
}

static std::vector<std::string> split(const std::string& s, char delimiter) {
    std::vector<std::string> values;
    std::stringstream stream(s);
    std::string item;
    while (std::getline(stream, item, delimiter)) values.push_back(item);
    return values;
}

static std::string clean(const std::string& value) {
    std::string result;
    for (unsigned char c : value) {
        if (std::isalnum(c) || c == '_') result.push_back(static_cast<char>(std::tolower(c)));
    }
    if (result.empty()) result = "asset";
    if (std::isdigit(static_cast<unsigned char>(result.front()))) result = "a_" + result;
    return result;
}

static std::string stem(const std::string& filename) {
    const auto dot = filename.find_last_of('.');
    return clean(dot == std::string::npos ? filename : filename.substr(0, dot));
}

static std::string quote(const std::string& value) {
    std::string result;
    for (char c : value) {
        if (c == '\\' || c == '"') result.push_back('\\');
        if (c == '\n') result += "\\n";
        else if (c == '\r') continue;
        else result.push_back(c);
    }
    return result;
}

static std::string literal(const std::string& value) {
    if (value.empty()) return "True";
    if (value == "True" || value == "False" || value == "None") return value;
    char* end = nullptr;
    std::strtod(value.c_str(), &end);
    if (end != value.c_str() && end && *end == '\0') return value;
    return "\"" + quote(value) + "\"";
}

static double number(const std::string& value, double fallback, double low, double high) {
    char* end = nullptr;
    const double parsed = std::strtod(value.c_str(), &end);
    if (end == value.c_str() || !end || *end != '\0') return fallback;
    return std::max(low, std::min(high, parsed));
}

static std::string transform(const std::string& x, const std::string& y,
                             const std::string& zoom, const std::string& rotation) {
    std::ostringstream out;
    out << "Transform(xalign=" << number(x, 0.5, 0.0, 1.0)
        << ", yalign=" << number(y, 0.62, 0.0, 1.0)
        << ", zoom=" << number(zoom, 0.9, 0.05, 4.0)
        << ", rotate=" << number(rotation, 0.0, -360.0, 360.0) << ")";
    return out.str();
}

struct Event {
    std::string type;
    std::vector<std::string> args;
};

static bool validOperator(const std::string& value) {
    return value == "==" || value == "!=" || value == ">" ||
           value == "<" || value == ">=" || value == "<=";
}

extern "C" JNIEXPORT jstring JNICALL
Java_studio_renpy_mobile_NativeBridge_compileStory(JNIEnv* env, jclass, jstring input) {
    const char* raw = env->GetStringUTFChars(input, nullptr);
    const std::string data(raw ? raw : "");
    if (raw) env->ReleaseStringUTFChars(input, raw);

    std::stringstream source(data);
    std::string line;
    std::vector<Event> events;
    while (std::getline(source, line)) {
        const auto fields = split(line, '|');
        if (fields.empty() || fields[0].empty()) continue;
        Event event;
        event.type = fields[0];
        for (size_t i = 1; i < fields.size(); ++i) event.args.push_back(b64(fields[i]));
        events.push_back(event);
    }

    const auto get = [](const Event& event, size_t index) -> std::string {
        return index < event.args.size() ? event.args[index] : "";
    };

    std::map<std::string, std::string> imageDefinitions;
    std::map<std::string, std::vector<std::string>> layeredDefinitions;
    std::set<std::string> speakerDefinitions;

    for (const auto& event : events) {
        if (event.type == "background") {
            const std::string imageName = "bg " + stem(get(event, 0));
            imageDefinitions[imageName] = "images/" + get(event, 0);
        } else if (event.type == "sprite") {
            const std::string imageName = clean(get(event, 0)) + " " + stem(get(event, 1));
            imageDefinitions[imageName] = "images/" + get(event, 1);
        } else if (event.type == "layered") {
            const std::string tag = clean(get(event, 0));
            if (layeredDefinitions.find(tag) == layeredDefinitions.end()) {
                layeredDefinitions[tag] = { get(event, 1), get(event, 2), get(event, 3) };
            }
        } else if (event.type == "say" && !get(event, 0).empty()) {
            const std::string tag = clean(get(event, 0));
            if (tag != "narrator") {
                speakerDefinitions.insert("define " + tag + " = Character(\"" + quote(get(event, 0)) + "\")");
            }
        }
    }

    std::string output =
        "# Generated by Ren'Py Studio Mobile.\n"
        "# Assets are referenced relative to the game/ directory.\n\n"
        "define config.name = \"A Visual Novel\"\n";

    for (const auto& definition : speakerDefinitions) output += definition + "\n";
    for (const auto& definition : imageDefinitions) {
        output += "image " + definition.first + " = \"" + quote(definition.second) + "\"\n";
    }

    for (const auto& definition : layeredDefinitions) {
        const auto& parts = definition.second;
        output += "\nlayeredimage " + definition.first + ":\n";
        output += "    group left:\n";
        output += "        attribute left_part default:\n";
        output += "            \"images/" + quote(parts[0]) + "\"\n";
        output += "    group right:\n";
        output += "        attribute right_part default:\n";
        output += "            \"images/" + quote(parts[1]) + "\"\n";
        output += "    group head:\n";
        output += "        attribute head_part default:\n";
        output += "            \"images/" + quote(parts[2]) + "\"\n";
    }

    output += "\nlabel start:\n";
    int indent = 1;
    int conditionalDepth = 0;
    bool ended = false;

    const auto pad = [&]() { return std::string(static_cast<size_t>(std::max(1, indent) * 4), ' '); };

    for (const auto& event : events) {
        const std::string& type = event.type;
        if (type == "if") {
            const std::string op = validOperator(get(event, 1)) ? get(event, 1) : "==";
            output += pad() + "if " + clean(get(event, 0)) + " " + op + " " + literal(get(event, 2)) + ":\n";
            ++indent;
            ++conditionalDepth;
        } else if (type == "else") {
            if (conditionalDepth > 0) {
                indent = std::max(1, indent - 1);
                output += pad() + "else:\n";
                ++indent;
            }
        } else if (type == "endif") {
            if (conditionalDepth > 0) {
                indent = std::max(1, indent - 1);
                --conditionalDepth;
            }
        } else if (type == "say") {
            const std::string speaker = clean(get(event, 0));
            const std::string text = quote(get(event, 1));
            output += pad() + (speaker == "narrator" || get(event, 0).empty()
                ? "\"" + text + "\""
                : speaker + " \"" + text + "\"") + "\n";
        } else if (type == "background") {
            const std::string imageName = "bg " + stem(get(event, 0));
            output += pad() + "scene " + imageName + " at " +
                transform(get(event, 1), get(event, 2), get(event, 3), get(event, 4)) + "\n";
        } else if (type == "sprite") {
            const std::string tag = clean(get(event, 0));
            const std::string imageName = tag + " " + stem(get(event, 1));
            output += pad() + "show " + imageName + " at " +
                transform(get(event, 2), get(event, 3), get(event, 4), get(event, 5)) + "\n";
        } else if (type == "layered") {
            const std::string tag = clean(get(event, 0));
            output += pad() + "show " + tag + " at " +
                transform(get(event, 4), get(event, 5), get(event, 6), get(event, 7)) + "\n";
        } else if (type == "choice") {
            output += pad() + "menu:\n";
            output += pad() + "    \"" + quote(get(event, 0)) + "\":\n";
            output += pad() + "        $ choice_result = \"A\"\n";
            output += pad() + "    \"" + quote(get(event, 1)) + "\":\n";
            output += pad() + "        $ choice_result = \"B\"\n";
        } else if (type == "set") {
            output += pad() + "$ " + clean(get(event, 0)) + " = " + literal(get(event, 1)) + "\n";
        } else if (type == "music") {
            output += pad() + "play music \"" + quote("audio/" + get(event, 0)) + "\" fadein 1.0\n";
        } else if (type == "sound") {
            output += pad() + "play sound \"" + quote("audio/" + get(event, 0)) + "\"\n";
        } else if (type == "stopmusic") {
            output += pad() + "stop music fadeout 1.0\n";
        } else if (type == "transition") {
            const std::string effect = clean(get(event, 0));
            const std::set<std::string> allowed = { "dissolve", "fade", "pixellate", "move", "None" };
            output += pad() + "with " + (allowed.count(effect) ? effect : "dissolve") + "\n";
        } else if (type == "pause") {
            output += pad() + "pause " + std::to_string(number(get(event, 0), 1.0, 0.0, 600.0)) + "\n";
        } else if (type == "hide") {
            output += pad() + "hide " + clean(get(event, 0)) + "\n";
        } else if (type == "label") {
            if (conditionalDepth == 0) {
                output += "\nlabel " + clean(get(event, 0)) + ":\n";
                indent = 1;
            }
        } else if (type == "jump") {
            output += pad() + "jump " + clean(get(event, 0)) + "\n";
        } else if (type == "call") {
            output += pad() + "call " + clean(get(event, 0)) + "\n";
        } else if (type == "plugin") {
            // Plugin code is supplied by an installed extension; only install trusted plugins.
            std::stringstream pluginSource(get(event, 2));
            std::string pluginLine;
            while (std::getline(pluginSource, pluginLine)) {
                if (pluginLine.empty()) output += "\n";
                else output += pad() + pluginLine + "\n";
            }
        } else if (type == "return" || type == "end") {
            output += pad() + "return\n";
            if (type == "end") ended = true;
        }
    }

    if (!ended) output += "    return\n";
    return env->NewStringUTF(output.c_str());
}
