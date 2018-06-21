package com.github.freeacs.springshell.commands;

import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.springshell.ShellContext;

public abstract class ShellCommands {

    protected final ShellContext shellContext;

    public ShellCommands(ShellContext shellContext) {
        this.shellContext = shellContext;
    }

    public String doOnProfile(CheckedBiFunction<Unittype, Profile, String> func) {
        return shellContext.getUnittype()
                .map(unittype -> shellContext.getProfile()
                        .map(profile -> func.apply(unittype, profile))
                        .orElseGet(() -> "Profile is not set"))
                .orElseGet(() -> "Unittype is not set");
    }

    public String doOnUnittype(CheckedFunction<Unittype, String> func) {
        return shellContext.getUnittype()
                .map(func::apply)
                .orElseGet(() -> "Unittype is not set");
    }

    @FunctionalInterface
    public interface CheckedFunction<T1, R> {
        R apply(T1 t1) throws IllegalArgumentException;
    }

    @FunctionalInterface
    public interface CheckedBiFunction<T1, T2, R> {
        R apply(T1 t1, T2 t2) throws IllegalArgumentException;
    }
}
