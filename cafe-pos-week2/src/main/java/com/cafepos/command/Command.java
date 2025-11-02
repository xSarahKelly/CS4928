package com.cafepos.command;

public interface Command {
    void execute();
    default void undo(){
        //optional to implement
    }
    /** 
     * Optional label used by CLI demos or debugging.
     * Not required by the pattern but nice for printing menus.
     */
    default String name() {
        return getClass().getSimpleName();
    }
}
