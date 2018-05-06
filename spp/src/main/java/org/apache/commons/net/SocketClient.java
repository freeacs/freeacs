/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;


/**
 * The SocketClient provides the basic operations that are required of
 * client objects accessing sockets.  It is meant to be
 * subclassed to avoid having to rewrite the same code over and over again
 * to open a socket, close a socket, set timeouts, etc.  Of special note
 * is the {@link #setSocketFactory  setSocketFactory }
 * method, which allows you to control the type of Socket the SocketClient
 * creates for initiating network connections.  This is especially useful
 * for adding SSL or proxy support as well as better support for applets.  For
 * example, you could create a
 * {@link org.apache.commons.net.SocketFactory} that
 * requests browser security capabilities before creating a socket.
 * All classes derived from SocketClient should use the
 * {@link #_socketFactory_  _socketFactory_ } member variable to
 * create Socket and ServerSocket instances rather than instanting
 * them by directly invoking a constructor.  By honoring this contract
 * you guarantee that a user will always be able to provide his own
 * Socket implementations by substituting his own SocketFactory.
 * @author Daniel F. Savarese
 * @see SocketFactory
 */
public abstract class SocketClient
{
    /**
     * The end of line character sequence used by most IETF protocols.  That
     * is a carriage return followed by a newline: "\r\n"
     */
    public static final String NETASCII_EOL = "\r\n";

    /** The default SocketFactory shared by all SocketClient instances. */
    private static final SocketFactory __DEFAULT_SOCKET_FACTORY =
            SocketFactory.getDefault();
    
    private static final ServerSocketFactory __DEFAULT_SERVER_SOCKET_FACTORY = 
            ServerSocketFactory.getDefault();

    /** The timeout to use after opening a socket. */
    protected int _timeout_;

    /** The socket used for the connection. */
    protected Socket _socket_;

    /** The default port the client should connect to. */
    protected int _defaultPort_;

    /** The socket's InputStream. */
    protected InputStream _input_;

    /** The socket's OutputStream. */
    protected OutputStream _output_;

    /** The socket's SocketFactory. */
    protected SocketFactory _socketFactory_;
    
    /** The socket's ServerSocket Factory. */
    protected ServerSocketFactory _serverSocketFactory_;
    
    /** The socket's connect timeout (0 = infinite timeout) */
    private static final int DEFAULT_CONNECT_TIMEOUT = 0;
    protected int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

    /**
     * Default constructor for SocketClient.  Initializes
     * _socket_ to null, _timeout_ to 0, _defaultPort to 0,
     * _isConnected_ to false, and _socketFactory_ to a shared instance of
     * {@link org.apache.commons.net.DefaultSocketFactory}.
     */
    public SocketClient()
    {
        _socket_ = null;
        _input_ = null;
        _output_ = null;
        _timeout_ = 0;
        _defaultPort_ = 0;
        _socketFactory_ = __DEFAULT_SOCKET_FACTORY;
        _serverSocketFactory_ = __DEFAULT_SERVER_SOCKET_FACTORY;
    }


    /**
     * Because there are so many connect() methods, the _connectAction_()
     * method is provided as a means of performing some action immediately
     * after establishing a connection, rather than reimplementing all
     * of the connect() methods.  The last action performed by every
     * connect() method after opening a socket is to call this method.
     * <p>
     * This method sets the timeout on the just opened socket to the default
     * timeout set by {@link #setDefaultTimeout  setDefaultTimeout() },
     * sets _input_ and _output_ to the socket's InputStream and OutputStream
     * respectively, and sets _isConnected_ to true.
     * <p>
     * Subclasses overriding this method should start by calling
     * <code> super._connectAction_() </code> first to ensure the
     * initialization of the aforementioned protected variables.
     */
    protected void _connectAction_() throws IOException
    {
        _socket_.setSoTimeout(_timeout_);
        _input_ = _socket_.getInputStream();
        _output_ = _socket_.getOutputStream();
    }


    /**
     * Opens a Socket connected to a remote host at the specified port and
     * originating from the current host at a system assigned port.
     * Before returning, {@link #_connectAction_  _connectAction_() }
     * is called to perform connection initialization actions.
     * <p>
     * @param host  The remote host.
     * @param port  The port to connect to on the remote host.
     * @exception SocketException If the socket timeout could not be set.
     * @exception IOException If the socket could not be opened.  In most
     *  cases you will only want to catch IOException since SocketException is
     *  derived from it.
     */
    public void connect(InetAddress host, int port)
    throws SocketException, IOException
    {
        _socket_ = _socketFactory_.createSocket();
        _socket_.connect(new InetSocketAddress(host, port), connectTimeout);

        _connectAction_();
    }

    /**
     * Opens a Socket connected to a remote host at the specified port and
     * originating from the current host at a system assigned port.
     * Before returning, {@link #_connectAction_  _connectAction_() }
     * is called to perform connection initialization actions.
     * <p>
     * @param hostname  The name of the remote host.
     * @param port  The port to connect to on the remote host.
     * @exception SocketException If the socket timeout could not be set.
     * @exception IOException If the socket could not be opened.  In most
     *  cases you will only want to catch IOException since SocketException is
     *  derived from it.
     * @exception UnknownHostException If the hostname cannot be resolved.
     */
    public void connect(String hostname, int port)
    throws SocketException, IOException
    {
        _socket_= _socketFactory_.createSocket();
        _socket_.connect(new InetSocketAddress(hostname, port), connectTimeout);
        
        _connectAction_();
    }


    /**
     * Opens a Socket connected to a remote host at the specified port and
     * originating from the specified local address and port.
     * Before returning, {@link #_connectAction_  _connectAction_() }
     * is called to perform connection initialization actions.
     * <p>
     * @param host  The remote host.
     * @param port  The port to connect to on the remote host.
     * @param localAddr  The local address to use.
     * @param localPort  The local port to use.
     * @exception SocketException If the socket timeout could not be set.
     * @exception IOException If the socket could not be opened.  In most
     *  cases you will only want to catch IOException since SocketException is
     *  derived from it.
     */
    public void connect(InetAddress host, int port,
                        InetAddress localAddr, int localPort)
    throws SocketException, IOException
    {
        _socket_ = _socketFactory_.createSocket();
        _socket_.bind(new InetSocketAddress(localAddr, localPort));
        _socket_.connect(new InetSocketAddress(host, port), connectTimeout);
        
        _connectAction_();
    }


    /**
     * Opens a Socket connected to a remote host at the specified port and
     * originating from the specified local address and port.
     * Before returning, {@link #_connectAction_  _connectAction_() }
     * is called to perform connection initialization actions.
     * <p>
     * @param hostname  The name of the remote host.
     * @param port  The port to connect to on the remote host.
     * @param localAddr  The local address to use.
     * @param localPort  The local port to use.
     * @exception SocketException If the socket timeout could not be set.
     * @exception IOException If the socket could not be opened.  In most
     *  cases you will only want to catch IOException since SocketException is
     *  derived from it.
     * @exception UnknownHostException If the hostname cannot be resolved.
     */
    public void connect(String hostname, int port,
                        InetAddress localAddr, int localPort)
    throws SocketException, IOException
    {
        _socket_ =
            _socketFactory_.createSocket(hostname, port, localAddr, localPort);
        _connectAction_();
    }


    /**
     * Opens a Socket connected to a remote host at the current default port
     * and originating from the current host at a system assigned port.
     * Before returning, {@link #_connectAction_  _connectAction_() }
     * is called to perform connection initialization actions.
     * <p>
     * @param host  The remote host.
     * @exception SocketException If the socket timeout could not be set.
     * @exception IOException If the socket could not be opened.  In most
     *  cases you will only want to catch IOException since SocketException is
     *  derived from it.
     */
    public void connect(InetAddress host) throws SocketException, IOException
    {
        connect(host, _defaultPort_);
    }


    /**
     * Opens a Socket connected to a remote host at the current default
     * port and originating from the current host at a system assigned port.
     * Before returning, {@link #_connectAction_  _connectAction_() }
     * is called to perform connection initialization actions.
     * <p>
     * @param hostname  The name of the remote host.
     * @exception SocketException If the socket timeout could not be set.
     * @exception IOException If the socket could not be opened.  In most
     *  cases you will only want to catch IOException since SocketException is
     *  derived from it.
     * @exception UnknownHostException If the hostname cannot be resolved.
     */
    public void connect(String hostname) throws SocketException, IOException
    {
        connect(hostname, _defaultPort_);
    }


    /**
     * Disconnects the socket connection.
     * You should call this method after you've finished using the class
     * instance and also before you call
     * {@link #connect connect() }
     * again.  _isConnected_ is set to false, _socket_ is set to null,
     * _input_ is set to null, and _output_ is set to null.
     * <p>
     * @exception IOException  If there is an error closing the socket.
     */
    public void disconnect() throws IOException
    {
        if (_socket_ != null) _socket_.close();
        if (_input_ != null) _input_.close();
        if (_output_ != null) _output_.close();
        if (_socket_ != null) _socket_ = null;
        _input_ = null;
        _output_ = null;
    }


    /**
     * Returns true if the client is currently connected to a server.
     * <p>
     * @return True if the client is currently connected to a server,
     *         false otherwise.
     */
    public boolean isConnected()
    {
        if (_socket_ == null)
            return false;
        
        return _socket_.isConnected();
    }


    /**
     * Sets the default port the SocketClient should connect to when a port
     * is not specified.  The {@link #_defaultPort_  _defaultPort_ }
     * variable stores this value.  If never set, the default port is equal
     * to zero.
     * <p>
     * @param port  The default port to set.
     */
    public void setDefaultPort(int port)
    {
        _defaultPort_ = port;
    }

    /**
     * Returns the current value of the default port (stored in
     * {@link #_defaultPort_  _defaultPort_ }).
     * <p>
     * @return The current value of the default port.
     */
    public int getDefaultPort()
    {
        return _defaultPort_;
    }


    /**
     * Set the default timeout in milliseconds to use when opening a socket.
     * This value is only used previous to a call to
     * {@link #connect connect()}
     * and should not be confused with {@link #setSoTimeout setSoTimeout()}
     * which operates on an the currently opened socket.  _timeout_ contains
     * the new timeout value.
     * <p>
     * @param timeout  The timeout in milliseconds to use for the socket
     *                 connection.
     */
    public void setDefaultTimeout(int timeout)
    {
        _timeout_ = timeout;
    }


    /**
     * Returns the default timeout in milliseconds that is used when
     * opening a socket.
     * <p>
     * @return The default timeout in milliseconds that is used when
     *         opening a socket.
     */
    public int getDefaultTimeout()
    {
        return _timeout_;
    }


    /**
     * Set the timeout in milliseconds of a currently open connection.
     * Only call this method after a connection has been opened
     * by {@link #connect connect()}.
     * <p>
     * @param timeout  The timeout in milliseconds to use for the currently
     *                 open socket connection.
     * @exception SocketException If the operation fails.
     */
    public void setSoTimeout(int timeout) throws SocketException
    {
        _socket_.setSoTimeout(timeout);
    }
    
    
    /**
     * Set the underlying socket send buffer size.
     * <p>
     * @param size The size of the buffer in bytes.
     * @throws SocketException 
     * @since 2.0
     */
    public void setSendBufferSize(int size) throws SocketException {
        _socket_.setSendBufferSize(size);
    }
    
    
    /**
     * Sets the underlying socket receive buffer size.
     * <p>
     * @param size The size of the buffer in bytes.
     * @throws SocketException 
     * @since 2.0
     */
    public void setReceiveBufferSize(int size) throws SocketException  {
        _socket_.setReceiveBufferSize(size);
    }


    /**
     * Returns the timeout in milliseconds of the currently opened socket.
     * <p>
     * @return The timeout in milliseconds of the currently opened socket.
     * @exception SocketException If the operation fails.
     */
    public int getSoTimeout() throws SocketException
    {
        return _socket_.getSoTimeout();
    }

    /**
     * Enables or disables the Nagle's algorithm (TCP_NODELAY) on the
     * currently opened socket.
     * <p>
     * @param on  True if Nagle's algorithm is to be enabled, false if not.
     * @exception SocketException If the operation fails.
     */
    public void setTcpNoDelay(boolean on) throws SocketException
    {
        _socket_.setTcpNoDelay(on);
    }


    /**
     * Returns true if Nagle's algorithm is enabled on the currently opened
     * socket.
     * <p>
     * @return True if Nagle's algorithm is enabled on the currently opened
     *        socket, false otherwise.
     * @exception SocketException If the operation fails.
     */
    public boolean getTcpNoDelay() throws SocketException
    {
        return _socket_.getTcpNoDelay();
    }


    /**
     * Sets the SO_LINGER timeout on the currently opened socket.
     * <p>
     * @param on  True if linger is to be enabled, false if not.
     * @param val The linger timeout (in hundredths of a second?)
     * @exception SocketException If the operation fails.
     */
    public void setSoLinger(boolean on, int val) throws SocketException
    {
        _socket_.setSoLinger(on, val);
    }


    /**
     * Returns the current SO_LINGER timeout of the currently opened socket.
     * <p>
     * @return The current SO_LINGER timeout.  If SO_LINGER is disabled returns
     *         -1.
     * @exception SocketException If the operation fails.
     */
    public int getSoLinger() throws SocketException
    {
        return _socket_.getSoLinger();
    }


    /**
     * Returns the port number of the open socket on the local host used
     * for the connection.
     * <p>
     * @return The port number of the open socket on the local host used
     *         for the connection.
     */
    public int getLocalPort()
    {
        return _socket_.getLocalPort();
    }


    /**
     * Returns the local address to which the client's socket is bound.
     * <p>
     * @return The local address to which the client's socket is bound.
     */
    public InetAddress getLocalAddress()
    {
        return _socket_.getLocalAddress();
    }

    /**
     * Returns the port number of the remote host to which the client is
     * connected.
     * <p>
     * @return The port number of the remote host to which the client is
     *         connected.
     */
    public int getRemotePort()
    {
        return _socket_.getPort();
    }


    /**
     * @return The remote address to which the client is connected.
     */
    public InetAddress getRemoteAddress()
    {
        return _socket_.getInetAddress();
    }


    /**
     * Verifies that the remote end of the given socket is connected to the
     * the same host that the SocketClient is currently connected to.  This
     * is useful for doing a quick security check when a client needs to
     * accept a connection from a server, such as an FTP data connection or
     * a BSD R command standard error stream.
     * <p>
     * @return True if the remote hosts are the same, false if not.
     */
    public boolean verifyRemote(Socket socket)
    {
        InetAddress host1, host2;

        host1 = socket.getInetAddress();
        host2 = getRemoteAddress();

        return host1.equals(host2);
    }


    /**
     * Sets the SocketFactory used by the SocketClient to open socket
     * connections.  If the factory value is null, then a default
     * factory is used (only do this to reset the factory after having
     * previously altered it).
     * <p>
     * @param factory  The new SocketFactory the SocketClient should use.
     */
    public void setSocketFactory(SocketFactory factory)
    {
        if (factory == null)
            _socketFactory_ = __DEFAULT_SOCKET_FACTORY;
        else
            _socketFactory_ = factory;
    }
    
    /**
     * Sets the ServerSocketFactory used by the SocketClient to open ServerSocket
     * connections.  If the factory value is null, then a default
     * factory is used (only do this to reset the factory after having
     * previously altered it).
     * <p>
     * @param factory  The new ServerSocketFactory the SocketClient should use.
     * @since 2.0
     */
    public void setServerSocketFactory(ServerSocketFactory factory) {
        if (factory == null)
            _serverSocketFactory_ = __DEFAULT_SERVER_SOCKET_FACTORY;
        else
            _serverSocketFactory_ = factory;
    }
    
    /**
     * Sets the connection timeout in milliseconds, which will be passed to the {@link Socket} object's
     * connect() method. 
     * @param connectTimeout The connection timeout to use (in ms)
     * @since 2.0
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    
    /**
     * Get the underlying socket connection timeout.
     * @return
     * @since 2.0
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    
    
}


