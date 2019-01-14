package com.example.chronos.finalproject;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.chronos.finalproject.MainMenu.IDUser;
import static com.example.chronos.finalproject.MainMenu.FullNameUser;

public class FriendsListFragment extends Fragment {

    // Arreglos para las llaves de los usuarios amigos y solicitantes de amistad
    ArrayList<String> friendsKeys = new ArrayList<>();
    ArrayList<String> requestersKeys = new ArrayList<>();

    // Variables para componentes de la UI globales para pder actualizar desde la funcion
    ArrayList<HashMap<String, String>> friendsList, requestsList;
    ListAdapter friendsListAdapter, reqListAdapter;

    // Limpiar arreglos de friendsList y friendsKeys, descargar amigos del usuario y notificar al friendsListAdapter de friendsListView
    public void updateFriendsList() {
        friendsList.clear();
        friendsKeys.clear();
        DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("Amigos/" + IDUser);
        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap<String, Object> userFriends = (HashMap<String, Object>) dataSnapshot.getValue();
                    for (String key : userFriends.keySet()) {
                        HashMap<String, String> friendListItem = new HashMap<>();
                        friendListItem.put("Friend", userFriends.get(key).toString());
                        friendsList.add(friendListItem);
                        friendsKeys.add(key);
                    }
                    ((SimpleAdapter) friendsListAdapter).notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Limpiar arreglos de requestsList y requestersKeys, descargar solicitudes del usuario y notificar al reqListAdapter de requestsListView
    public void updateRequestsList() {
        requestsList.clear();
        requestersKeys.clear();
        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("Solicitudes/" + IDUser);
        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap<String, Object> userRequests = (HashMap<String, Object>) dataSnapshot.getValue();
                    for (String key : userRequests.keySet()) {
                        HashMap<String, String> requestListItem = new HashMap<>();
                        requestListItem.put("Request", userRequests.get(key).toString());
                        requestsList.add(requestListItem);
                        requestersKeys.add(key);
                    }
                    ((SimpleAdapter) reqListAdapter).notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicializar componentes de la UI
        View rootView = inflater.inflate(R.layout.fragment_friends_list, container, false);
        Button friendsButton = rootView.findViewById(R.id.friendsButton);
        Button reqButton = rootView.findViewById(R.id.reqButton);

        friendsList = new ArrayList<>();
        final ListView friendsListView = rootView.findViewById(R.id.friendsListView);
        friendsListAdapter = new SimpleAdapter(
                getContext(),
                friendsList,
                R.layout.friend_req_list_item,
                new String[]{"Friend"},
                new int[]{R.id.friendReqTextView});
        friendsListView.setAdapter(friendsListAdapter);

        requestsList = new ArrayList<>();
        final ListView requestsListView = rootView.findViewById(R.id.reqListView);
        reqListAdapter = new SimpleAdapter(
                getContext(),
                requestsList,
                R.layout.friend_req_list_item,
                new String[]{"Request"},
                new int[]{R.id.friendReqTextView});
        requestsListView.setAdapter(reqListAdapter);

        // Descargar amigos del usuario
        updateFriendsList();

        // Descargar solicitudes del usuario
        updateRequestsList();

        // click en una solicitud para aceptar
        requestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.request_decision))
                        .setMessage(getString(R.string.request_message))
                        .setPositiveButton(getString(R.string.request_accepted), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Agregar amigo a la lista del usuario y del amigo aceptado y TODO borrar solicitud
                                DatabaseReference newFriendRef = FirebaseDatabase.getInstance().getReference("Amigos/" + IDUser + "/" + requestersKeys.get(position));
                                newFriendRef.setValue(requestsList.get(position).get("Request"));
                                DatabaseReference forFriendRef = FirebaseDatabase.getInstance().getReference("Amigos/" + requestersKeys.get(position) + "/" + IDUser);
                                forFriendRef.setValue(FullNameUser);
                                DatabaseReference eraseReqRef = FirebaseDatabase.getInstance().getReference("Solicitudes/" + IDUser + "/" + requestersKeys.get(position));
                                eraseReqRef.setValue(null);
                                Toast.makeText(getContext(), getString(R.string.friend_added), Toast.LENGTH_SHORT).show();
                                updateFriendsList();
                                updateRequestsList();
                            }
                        })
                        .setNegativeButton(R.string.request_rejected, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Borrar solicitud
                                DatabaseReference eraseReqRef = FirebaseDatabase.getInstance().getReference("Solicitudes/" + IDUser + "/" + requestersKeys.get(position));
                                eraseReqRef.setValue(null);
                                Toast.makeText(getContext(), getString(R.string.request_erased), Toast.LENGTH_SHORT).show();
                                updateRequestsList();
                            }
                        })
                        .show();
            }
        });

        // Botones para mostrar amigos o solicitudes
        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendsListView.setVisibility(View.VISIBLE);
                requestsListView.setVisibility(View.INVISIBLE);
            }
        });
        reqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestsListView.setVisibility(View.VISIBLE);
                friendsListView.setVisibility(View.INVISIBLE);
            }
        });

        return rootView;
    }
}
