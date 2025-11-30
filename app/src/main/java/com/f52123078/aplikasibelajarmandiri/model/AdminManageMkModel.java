package com.f52123078.aplikasibelajarmandiri.model;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;

public class AdminManageMkModel {

    public interface DataListener {
        void onProdiLoaded(List<Prodi> list);
        void onMkLoaded(List<MataKuliah> list);
        void onWriteSuccess(String message);
        void onError(String error);
    }

    private final CollectionReference prodiCol;
    private final CollectionReference mkCol;
    private ListenerRegistration mkListener;

    public AdminManageMkModel() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        prodiCol = db.collection("prodi");
        mkCol = db.collection("mata_kuliah");
    }

    public void loadProdi(DataListener listener) {
        prodiCol.orderBy("name").get()
                .addOnSuccessListener(s -> listener.onProdiLoaded(s.toObjects(Prodi.class)))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void loadMkRealtime(DataListener listener) {
        if (mkListener != null) mkListener.remove();
        mkListener = mkCol.orderBy("name").addSnapshotListener((s, e) -> {
            if (e != null) { listener.onError(e.getMessage()); return; }
            if (s != null) listener.onMkLoaded(s.toObjects(MataKuliah.class));
        });
    }

    public void addMk(MataKuliah mk, DataListener listener) {
        mkCol.add(mk)
                .addOnSuccessListener(v -> listener.onWriteSuccess("MK ditambahkan"))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void updateMk(String id, MataKuliah mk, DataListener listener) {
        mkCol.document(id).set(mk) // set() menimpa data
                .addOnSuccessListener(v -> listener.onWriteSuccess("MK diperbarui"))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void deleteMk(String id, DataListener listener) {
        mkCol.document(id).delete()
                .addOnSuccessListener(v -> listener.onWriteSuccess("MK dihapus"))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void detachListener() { if (mkListener != null) mkListener.remove(); }
}