package com.pw.awairbridge.persistence;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FirebasePersistence implements TokenPersistence{

  final Firestore firestore;
  final String initialRefreshToken;

  public FirebasePersistence(Firestore firestore, String initialRefreshToken) {
    this.firestore = firestore;
    this.initialRefreshToken = initialRefreshToken;
  }

  @Override
  public void store(String refreshToken) {
    DocumentReference docRef = this.firestore.collection("token").document("awair");
    Map<String, Object> data = new HashMap<>();
    data.put("refreshToken", refreshToken);
    ApiFuture<WriteResult> result = docRef.set(data);
    try {
      WriteResult wr = result.get();
    } catch (Exception e) {
      log.error("Unable to write to firebase", e);
    }
  }

  @Override
  public String load() {
    DocumentReference docRef = firestore.collection("token").document("awair");
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document;
    String result = this.initialRefreshToken;
    try {
      document = future.get();

      if (document.exists()) {
        result = (String) document.getData().get("refreshToken");
      }
    } catch (Exception e) {
      log.error("Unable to read from firebase", e);
    }

    return result;
  }
}
