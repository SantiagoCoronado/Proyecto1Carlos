package com.example.chronos.finalproject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.chronos.finalproject.MainMenu.IDUser;

public class PostsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicializa la vista de fragment_posts y elementos de la lista
        View rootView = inflater.inflate(R.layout.fragment_posts, container, false);

        // Inicializar arreglos de posts para la listView
        final ArrayList<HashMap<String, String>> allPosts = new ArrayList<>();
        final ListView postsListView = rootView.findViewById(R.id.postsListView);
        final ListAdapter postsListAdapter = new SimpleAdapter(
                getContext(),
                allPosts,
                R.layout.post_list_item,
                new String[]{"UserName","EventName", "Content"},
                new int[]{R.id.postUser,R.id.postEvent, R.id.postContent});
        postsListView.setAdapter(postsListAdapter);

        // Inicializar arreglo de HashMaps que contendra toda la informacion de cada post
        final ArrayList<HashMap<String, Object>> fullPostsInfo = new ArrayList<>();

        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Publicaciones");
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot data: dataSnapshot.getChildren()) {
                        HashMap<String, Object> singlePost = (HashMap<String, Object>) data.getValue();
                        fullPostsInfo.add(0, singlePost);
                        HashMap<String, String> listPost = new HashMap<>();
                        listPost.put("UserName", singlePost.get("NombreUsuario").toString());
                        listPost.put("EventName", singlePost.get("NombreEvento").toString());
                        listPost.put("Content", singlePost.get("Contenido").toString());
                        allPosts.add(0, listPost);
                        ((SimpleAdapter) postsListAdapter).notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("TestApp", String.valueOf(position));
                Log.i("TestApp", allPosts.toString());

                if (!fullPostsInfo.get(position).get("IDUsuario").equals(IDUser)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("ForeignUserID", fullPostsInfo.get(position).get("IDUsuario").toString());
                    ForeignProfile foreignProfile = new ForeignProfile();
                    foreignProfile.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.placeHolderFrameLayout, foreignProfile)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        return rootView;

    }

 }
