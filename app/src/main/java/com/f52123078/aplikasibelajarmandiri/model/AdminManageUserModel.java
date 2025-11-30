package com.f52123078.aplikasibelajarmandiri.model;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;

public class AdminManageUserModel {

    public interface DataListener {
        void onUsersLoaded(List<User> userList);
        void onError(String error);
    }

    private final CollectionReference usersCol;
    private ListenerRegistration userListener;

    public AdminManageUserModel() {
        usersCol = FirebaseFirestore.getInstance().collection("users");
    }

    public void loadUsersRealtime(DataListener listener) {
        if (userListener != null) userListener.remove();

        // Mengurutkan berdasarkan nama
        userListener = usersCol.orderBy("name").addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                listener.onError(e.getMessage());
                return;
            }
            if (snapshot != null) {
                listener.onUsersLoaded(snapshot.toObjects(User.class));
            }
        });
    }

    public void detachListener() {
        if (userListener != null) userListener.remove();
    }
}