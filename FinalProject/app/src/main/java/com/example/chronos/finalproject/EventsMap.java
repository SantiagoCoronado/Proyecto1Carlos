package com.example.chronos.finalproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class EventsMap extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    Button showDetailsButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicializar vista raiz y elementos para el fragmento del mapa desde fragment_events_map.xml
        final View rootView = inflater.inflate(R.layout.fragment_events_map, container, false);
        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        // Inflater para obtener elementos xml del popup y poder modificarlos
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View detailsView = layoutInflater.inflate(R.layout.popup_details_event, null);
        final TextView eventNameTextView = detailsView.findViewById(R.id.eventNameTextView);
        final TextView organizerNameTextView = detailsView.findViewById(R.id.organizerNameTextView);
        final TextView dateEventTextView = detailsView.findViewById(R.id.dateEventTextView);
        final TextView quotaEventTextView = detailsView.findViewById(R.id.quotaEventTextView);
        final PopupWindow popupWindow = new PopupWindow(detailsView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        showDetailsButton = detailsView.findViewById(R.id.showDetailsButton);

        // ArrayList para contener a todos los eventos y eventIndex para guardar indice
        final ArrayList<HashMap<String, Object>> eventsList = new ArrayList<>();
        final int[] eventIndex = {0};

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TODO: diferenciar iconos de eventos llenos y quitar opcion de seleccionar si ya esta lleno el cupo
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // Solicitar permisos
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
                googleMap.setMyLocationEnabled(true);

                //mostrar todos los eventos
                DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("Eventos");
                eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                HashMap<String, Object> eventValues = (HashMap<String, Object>) data.getValue();
                                eventValues.put("IDEvento", data.getKey());
                                eventsList.add(eventValues);
                                double latitude = (double) eventValues.get("Latitud");
                                double longitude = (double) eventValues.get("Longitud");
                                LatLng latLng = new LatLng(latitude, longitude);
                                googleMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                // Listener para los marcadores
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        double markerLatitude = marker.getPosition().latitude;
                        double markerLongitude = marker.getPosition().longitude;
                        for (int i = 0; i < eventsList.size(); i++) {
                            HashMap<String, Object> singleEvent = eventsList.get(i);
                            if (markerLatitude == (double) singleEvent.get("Latitud") && markerLongitude == (double) singleEvent.get("Longitud")) {
                                eventNameTextView.setText(singleEvent.get("Nombre").toString());
                                organizerNameTextView.setText(getString(R.string.organizer_name) + " " + singleEvent.get("NombreEncargado").toString());
                                dateEventTextView.setText(getString(R.string.date_event) + " " + singleEvent.get("DiaEvento").toString() + "/" + singleEvent.get("MesEvento").toString() + "/" + singleEvent.get("AnioEvento").toString());
                                quotaEventTextView.setText(getString(R.string.quota) + " " + singleEvent.get("Cupo").toString());
                                eventIndex[0] = i;
                            }
                        }
                        popupWindow.setAnimationStyle(R.style.popup_window_animation);
                        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
                        popupWindow.setFocusable(true);
                        popupWindow.setOutsideTouchable(true);
                        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
                        return true;
                    }
                });

                // Funcion para el boton que inicia el fragmento de detalles y le pasa los datos del evento
                showDetailsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("selectedEvent", eventsList.get(eventIndex[0]));
                        EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
                        eventDetailsFragment.setArguments(bundle);
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.placeHolderFrameLayout, eventDetailsFragment)
                                .addToBackStack(null)
                                .commit();
                        popupWindow.dismiss();
                    }
                });

                // Para zooming automaticamente en el zocalo de la ciudad de Oaxaca
                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(17.0436248,-96.7119411)).zoom(13).build();
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            try {
                mMapView.onDestroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }
}
