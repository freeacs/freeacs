/*
 * This file is part of JSTUN.
 *
 * Copyright (c) 2005 Thomas King <king@t-king.de> - All rights
 * reserved.
 *
 * This software is licensed under either the GNU Public License (GPL),
 * or the Apache 2.0 license. Copies of both license agreements are
 * included in this distribution.
 */
package de.javawi.jstun.header;

public interface MessageHeaderInterface {
  enum MessageHeaderType {
    BindingRequest,
    BindingResponse,
    BindingErrorResponse,
    SharedSecretRequest,
    SharedSecretResponse,
    SharedSecretErrorResponse
  }

  int BINDINGREQUEST = 0x0001;
  int BINDINGRESPONSE = 0x0101;
  int BINDINGERRORRESPONSE = 0x0111;
  int SHAREDSECRETREQUEST = 0x0002;
  int SHAREDSECRETRESPONSE = 0x0102;
  int SHAREDSECRETERRORRESPONSE = 0x0112;
}
