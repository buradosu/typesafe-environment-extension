package com.github.buradosu.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class EnvironmentConfig {

    private final String prefix;

    public EnvironmentConfig(String prefix) {
        this.prefix = prefix;
    }

    public Config getConfig() {
        Map<String, String> filteredMap = System.getenv()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, String> transformedKeys = new HashMap<>(filteredMap).entrySet().stream()
                .peek(entry -> entry.setValue(envVariableAsProperty(entry.getKey(), prefix)))
                .collect(Collectors.toMap(entry -> entry.getValue().toLowerCase(Locale.ROOT), entry -> filteredMap.get(entry.getKey())));
        return ConfigFactory.parseMap(transformedKeys, "from environment with prefix: " + prefix);
    }

    static String envVariableAsProperty(String variable, String prefix) throws ConfigException {
        StringBuilder builder = new StringBuilder();

        String strippedPrefix = variable.substring(prefix.length(), variable.length());

        int underscores = 0;
        for (char c : strippedPrefix.toCharArray()) {
            if (c == '_') {
                underscores++;
            } else {
                if (underscores > 0 && underscores < 4) {
                    builder.append(underscoreMappings(underscores));
                } else if (underscores > 3) {
                    throw new ConfigException.BadPath(variable, "Environment variable contains an un-mapped number of underscores.");
                }
                underscores = 0;
                builder.append(c);
            }
        }

        if (underscores > 0 && underscores < 4) {
            builder.append(underscoreMappings(underscores));
        } else if (underscores > 3) {
            throw new ConfigException.BadPath(variable, "Environment variable contains an un-mapped number of underscores.");
        }

        return builder.toString();
    }

    private static char underscoreMappings(int num) {
        // Rationale on name mangling:
        //
        // Most shells (e.g. bash, sh, etc.) doesn't support any character other
        // than alphanumeric and `_` in environment variables names.
        // In HOCON the default separator is `.` so it is directly translated to a
        // single `_` for convenience; `-` and `_` are less often present in config
        // keys but they have to be representable and the only possible mapping is
        // `_` repeated.
        switch (num) {
            case 1:
                return '.';
            case 2:
                return '-';
            case 3:
                return '_';
            default:
                return 0;
        }
    }
}
