package de.javawi.jstun.test.demo;

public class Counter {
  int request;
  int requestBinding;
  int requestBindingNoChange;
  int requestBindingIPChange;
  int requestBindingPortChange;
  int requestBindingIPPortChange;
  int requestBindingConnection;
  int requestUnknown;
  int messageAttributeChangeRequest;
  int kick;
  int idle;
  long receiveTimeNs;
  long processTimeMs;
  int error;

  public Counter cloneAndReset() {
    Counter clone = new Counter();
    clone.request = this.request;
    clone.requestBinding = this.requestBinding;
    clone.requestBindingNoChange = this.requestBindingNoChange;
    clone.requestBindingIPChange = this.requestBindingIPChange;
    clone.requestBindingPortChange = this.requestBindingPortChange;
    clone.requestBindingIPPortChange = this.requestBindingIPPortChange;
    clone.requestBindingConnection = this.requestBindingConnection;
    clone.requestUnknown = this.requestUnknown;
    clone.messageAttributeChangeRequest = this.messageAttributeChangeRequest;
    clone.kick = this.kick;
    clone.idle = this.idle;
    clone.receiveTimeNs = this.receiveTimeNs;
    clone.processTimeMs = this.processTimeMs;
    clone.error = this.error;
    this.request = 0;
    this.requestBinding = 0;
    this.requestBindingNoChange = 0;
    this.requestBindingIPChange = 0;
    this.requestBindingPortChange = 0;
    this.requestBindingIPPortChange = 0;
    this.requestBindingConnection = 0;
    this.requestUnknown = 0;
    this.messageAttributeChangeRequest = 0;
    this.kick = 0;
    this.idle = 0;
    this.receiveTimeNs = 0;
    this.processTimeMs = 0;
    this.error = 0;

    return clone;
  }

  public void incRequest() {
    request++;
  }

  public void incRequestBinding() {
    requestBinding++;
  }

  public void incRequestBindingNoChange() {
    requestBindingNoChange++;
  }

  public void incRequestBindingIPChange() {
    requestBindingIPChange++;
  }

  public void incRequestBindingPortChange() {
    requestBindingPortChange++;
  }

  public void incRequestBindingIPPortChange() {
    requestBindingIPPortChange++;
  }

  public void incRequestBindingConnection() {
    requestBindingConnection++;
  }

  public void incMessageAttributeChangeRequest() {
    messageAttributeChangeRequest++;
  }

  public void incRequestUnknown() {
    requestUnknown++;
  }

  public void incError() {
    error++;
  }

  public void incKick() {
    kick++;
  }

  public void incIdle() {
    idle++;
  }

  public void incReceiveTime(long ns) {
    receiveTimeNs += ns;
  }

  public void incProcessTimeMs(long ms) {
    processTimeMs += ms;
  }

  public int getRequest() {
    return request;
  }

  public int getRequestBinding() {
    return requestBinding;
  }

  public int getRequestBindingNoChange() {
    return requestBindingNoChange;
  }

  public int getRequestBindingIPChange() {
    return requestBindingIPChange;
  }

  public int getRequestBindingPortChange() {
    return requestBindingPortChange;
  }

  public int getRequestBindingIPPortChange() {
    return requestBindingIPPortChange;
  }

  public int getRequestBindingConnection() {
    return requestBindingConnection;
  }

  public int getRequestUnknown() {
    return requestUnknown;
  }

  public int getMessageAttributeChangeRequest() {
    return messageAttributeChangeRequest;
  }

  public int getKick() {
    return kick;
  }

  public int getIdle() {
    return idle;
  }

  public long getReceiveTimeNs() {
    return receiveTimeNs;
  }

  public long getProcessTimeMs() {
    return processTimeMs;
  }

  public int getError() {
    return error;
  }
}
