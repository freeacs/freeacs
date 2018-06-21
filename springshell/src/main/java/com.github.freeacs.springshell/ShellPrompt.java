package com.github.freeacs.springshell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class ShellPrompt implements PromptProvider {
    private final ShellContext shellContext;

    @Autowired
    public ShellPrompt(ShellContext shellContext) {
        this.shellContext = shellContext;
    }

    @Override
    public AttributedString getPrompt() {
        if (shellContext.getUnitType() != null) {
            return new AttributedString("freeacs-shell:(" + shellContext.getUnitType() + ":ut)>", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLACK));
        }
        return new AttributedString("freeacs-shell:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLACK));
    }
}