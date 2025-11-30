package com.f52123078.aplikasibelajarmandiri.model;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class AdminManageProdiModel {

    public interface DataListener {
        void onDataLoaded(List<Prodi> list);
        void onWriteSuccess(String message);
        void onError(String error);
    }

    private final CollectionReference prodiCol;
    private ListenerRegistration listenerRegistration;

    public AdminManageProdiModel() {
        prodiCol = FirebaseFirestore.getInstance().collection("prodi");
    }

    public void loadDataRealtime(DataListener listener) {
        if (listenerRegistration != null) listenerRegistration.remove();
        listenerRegistration = prodiCol.orderBy("name").addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                listener.onError(e.getMessage());
                return;
            }
            if (snapshot != null) {
                listener.onDataLoaded(snapshot.toObjects(Prodi.class));
            }
        });
    }

    public void addProdi(Prodi prodi, DataListener listener) {
        prodiCol.add(prodi)
                .addOnSuccessListener(doc -> listener.onWriteSuccess("Prodi berhasil ditambahkan"))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void updateProdi(String id, String newName, DataListener listener) {
        prodiCol.document(id).update("name", newName)
                .addOnSuccessListener(v -> listener.onWriteSuccess("Prodi diperbarui"))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void deleteProdi(String id, DataListener listener) {
        prodiCol.document(id).delete()
                .addOnSuccessListener(v -> listener.onWriteSuccess("Prodi dihapus"))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void detachListener() {
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}