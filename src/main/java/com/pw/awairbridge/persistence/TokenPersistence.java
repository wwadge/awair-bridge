package com.pw.awairbridge.persistence;

public interface TokenPersistence {

  void store(String refreshToken);
  String load();

}
