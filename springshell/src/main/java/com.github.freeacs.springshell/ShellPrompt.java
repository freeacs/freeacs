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
        return new AttributedString("freeacs-shell:" + shellContext.toString() + ">", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLACK));
    }
}