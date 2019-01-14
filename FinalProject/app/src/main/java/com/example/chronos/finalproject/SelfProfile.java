package com.example.chronos.finalproject;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.chronos.finalproject.MainMenu.IDUser;

public class SelfProfile extends Fragment {

    // Variables para publicaciones
    String fullUserName;
    ArrayList<Map<String, Object>> posiblePostEventList = new ArrayList<>();
    Map<String, Object> lastSelectedEvent;

    public Bitmap stringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicializar fragmento desde fragment_self_profile.xml junto con elementos de la UI
        View rootView = inflater.inflate(R.layout.fragment_self_profile, container, false);
        final TextView profNameTextView = rootView.findViewById(R.id.profNameTextView);
        final TextView profAgeTextView = rootView.findViewById(R.id.profAgeTextView);
        final ImageView profPicImageView = rootView.findViewById(R.id.profPicImageView);
        final Button prevEvnButton = rootView.findViewById(R.id.prevEvnButton);
        final Button nextEvButton = rootView.findViewById(R.id.nextEvButton);
        Button friendsProfButton = rootView.findViewById(R.id.friendsProfButton);

        // Guardar color por defecto del texto e inicializar boton preEvnButton
        final ColorStateList defaultBtnColors = prevEvnButton.getTextColors();
        prevEvnButton.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));

        // Inicializar la vista para las ventanas popup para publicaciones sobre eventos asistidos
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View postView = layoutInflater.inflate(R.layout.popup_new_post, null);
        final EditText popupPostEditText = postView.findViewById(R.id.popupPostEditText);
        final Button popupPostButton = postView.findViewById(R.id.popupPostButton);
        final PopupWindow popupWindow = new PopupWindow(postView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Inicializar listas de eventos previos y proximos, arreglos que serviran para llenarla y adaptadores
        final ArrayList<String> IDEvents = new ArrayList<>();

        final ArrayList<HashMap<String, String>> prevEvents = new ArrayList<>();
        final ListView prevEventsListView = rootView.findViewById(R.id.prevEventsListView);
        final ListAdapter prevEvListAdapter = new SimpleAdapter(
                getContext(),
                prevEvents,
                R.layout.user_events_list_item,
                new String[]{"EventName","EventDate"},
                new int[]{R.id.userEventNameTextView,R.id.userEventDateTextView});
        prevEventsListView.setAdapter(prevEvListAdapter);

        final ArrayList<HashMap<String, String>> nextEvents = new ArrayList<>();
        final ListView nextEventsListView = rootView.findViewById(R.id.nextEventsListView);
        final ListAdapter nextEvListAdapter = new SimpleAdapter(
                getContext(),
                nextEvents,
                R.layout.user_events_list_item,
                new String[]{"EventName","EventDate"},
                new int[]{R.id.userEventNameTextView,R.id.userEventDateTextView});
        nextEventsListView.setAdapter(nextEvListAdapter);

        // Obtener edad y nombre
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Usuarios/" + IDUser);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    try {
                        Map<String, Object> userValues = (HashMap<String, Object>) dataSnapshot.getValue();
                        profNameTextView.setText(userValues.get("Nombre").toString());
                        fullUserName = userValues.get("Nombre").toString() + " " + userValues.get("ApellidoPat") + " " + userValues.get("ApellidoMat").toString();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        String birthDay = (String) userValues.get("DiaNac");
                        String birthMonth = (String) userValues.get("MesNac");
                        String birthYear = (String) userValues.get("AnioNac");
                        Date actualDate = new Date();
                        Date userDate = dateFormat.parse(birthYear + "/" + birthMonth + "/" + birthDay);
                        long userAge = (actualDate.getTime() - userDate.getTime()) / 31536000000L;
                        profAgeTextView.setText(String.valueOf(userAge) + " " + getString(R.string.age));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Obtener imagen de perfil
        DatabaseReference profPicRef = FirebaseDatabase.getInstance().getReference("Usuarios-FotosPerfil/" + IDUser);
        profPicRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    Map<String, Object> userImage = (HashMap<String, Object>) dataSnapshot.getValue();
                    String imageString = userImage.get("fotoPerfil").toString();
                    profPicImageView.setImageBitmap(stringToBitMap(imageString));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Obtener lista de eventos y añadir el evento donde se encuentre el usuario
        DatabaseReference userEventsRef = FirebaseDatabase.getInstance().getReference("Usuarios-Eventos");
        userEventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Map<String, Object> eventAssistants = (HashMap<String, Object>) data.getValue();
                        if (eventAssistants.containsKey(IDUser)) {
                            IDEvents.add(data.getKey());
                        }
                    }
                }
                for (String IDEvent : IDEvents) {
                    DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("Eventos/" + IDEvent);
                    eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                try {
                                    Map<String, Object> eventInfo = (HashMap<String, Object>) dataSnapshot.getValue();
                                    eventInfo.put("IDEvent", dataSnapshot.getKey());
                                    HashMap<String, String> singleEvent = new HashMap<>();
                                    singleEvent.put("EventName", eventInfo.get("Nombre").toString());
                                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                    String eventDay = eventInfo.get("DiaEvento").toString();
                                    String eventMonth = eventInfo.get("MesEvento").toString();
                                    String eventYear = eventInfo.get("AnioEvento").toString();
                                    String eventStart = eventInfo.get("HoraInicial").toString();
                                    Date eventDate = dateFormat.parse(eventDay + "/" + eventMonth + "/" + eventYear + " " + eventStart + ":00:00");
                                    Date actualDate = new Date();
                                    long eventFlag = eventDate.getTime() - actualDate.getTime();
                                    singleEvent.put("EventDate", dateFormat.format(eventDate));
                                    if (Long.signum(eventFlag) == 1) {
                                        nextEvents.add(singleEvent);
                                        ((SimpleAdapter) nextEvListAdapter).notifyDataSetChanged();
                                    } else {
                                        prevEvents.add(singleEvent);
                                        posiblePostEventList.add(eventInfo);
                                        ((SimpleAdapter) prevEvListAdapter).notifyDataSetChanged();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // click en cada listItem para opinar y click en el boton para publicar
        prevEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastSelectedEvent = posiblePostEventList.get(position);
                popupPostEditText.setText("");
                popupPostButton.setEnabled(false);
                popupWindow.setAnimationStyle(R.style.popup_window_animation);
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
            }
        });
        popupPostEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    popupPostButton.setEnabled(true);
                } else {
                    popupPostButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        popupPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Publicaciones");
                String key = String.valueOf(new Date().getTime());
                Map<String, Object> newPost = new HashMap<>();
                newPost.put(key + "/Contenido/", popupPostEditText.getText().toString());
                newPost.put(key + "/NombreUsuario", fullUserName);
                newPost.put(key + "/NombreEvento/", lastSelectedEvent.get("Nombre"));
                newPost.put(key + "/IDUsuario/", IDUser);
                newPost.put(key + "/IDEvento/", lastSelectedEvent.get("IDEvent"));
                postRef.updateChildren(newPost);
            }
        });

        // longClick en cada listItem para eliminar, solo eventos por ocurrir
        nextEventsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: poder elimiar evento
                return false;
            }
        });

        // botones para cambiar listas
        prevEvnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevEvnButton.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
                prevEventsListView.setVisibility(View.VISIBLE);
                nextEvButton.setTextColor(defaultBtnColors);
                nextEventsListView.setVisibility(View.INVISIBLE);
            }
        });
        nextEvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextEvButton.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
                nextEventsListView.setVisibility(View.VISIBLE);
                prevEvnButton.setTextColor(defaultBtnColors);
                prevEventsListView.setVisibility(View.INVISIBLE);
            }
        });

        // Boton para ir a la lista de amigos y solicitudes
        friendsProfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendsListFragment friendsListFragment = new FriendsListFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.placeHolderFrameLayout, friendsListFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return rootView;
    }
}
