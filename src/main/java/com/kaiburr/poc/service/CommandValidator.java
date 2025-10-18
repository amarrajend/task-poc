// service/CommandValidator.java

package com.kaiburr.poc.service;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CommandValidator {

    private static final Set<String> ALLOWED_CMDS = new HashSet<>(Arrays.asList(
            "echo", "date", "whoami", "uname", "ls", "pwd", "printenv"
    ));

    private static final Set<Character> FORBIDDEN_CHARS = new HashSet<>(Arrays.asList(
            ';','|','&','>','<','`','$','(',')','{','}','*','?','~','\\'
    ));

    public void validateOrThrow(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Command must not be blank");
        }
        if (raw.length() > 512) {
            throw new IllegalArgumentException("Command too long");
        }
        for (char c : raw.toCharArray()) {
            if (FORBIDDEN_CHARS.contains(c)) {
                throw new IllegalArgumentException("Command contains forbidden characters");
            }
        }
        List<String> tokens = Arrays.asList(raw.trim().split("\\s+"));
        String cmd = tokens.get(0);
        if (!ALLOWED_CMDS.contains(cmd)) {
            throw new IllegalArgumentException("Command not permitted: " + cmd);
        }
    }

    public List<String> tokenize(String raw) {
        return Arrays.asList(raw.trim().split("\\s+"));
    }
}

