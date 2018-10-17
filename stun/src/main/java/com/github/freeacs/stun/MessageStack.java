package com.github.freeacs.stun;

import java.net.DatagramPacket;
import java.util.Stack;

public class MessageStack {
  private static Stack<DatagramPacket> stack = new Stack<>();

  public static DatagramPacket pop() {
    try {
      if (stack != null) {
        return stack.pop();
      } else {
        return null;
      }
    } catch (Exception ese) {
      return null;
    }
  }

  public static void push(DatagramPacket packet) {
    stack.push(packet);
  }
}
