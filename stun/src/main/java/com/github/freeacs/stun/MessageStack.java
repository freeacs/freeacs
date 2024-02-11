package com.github.freeacs.stun;

import java.net.DatagramPacket;
import java.util.EmptyStackException;
import java.util.Stack;

public class MessageStack {
  private static final Stack<DatagramPacket> stack = new Stack<>();

  public static DatagramPacket pop() {
    try {
      return stack.pop();
    } catch (EmptyStackException ese) {
      return null;
    }
  }

  public static void push(DatagramPacket packet) {
    stack.push(packet);
  }
}
