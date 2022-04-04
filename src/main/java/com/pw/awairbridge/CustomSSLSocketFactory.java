package com.pw.awairbridge;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;

public class SSLSocketFactoryEx extends SSLSocketFactory {

  @Override
  public String[] getDefaultCipherSuites() {
    return new String[0];
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return new String[0];
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose)
      throws IOException {
    return null;
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return null;
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException, UnknownHostException {
    return null;
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    return null;
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    return null;
  }
}
