package com.example.preiotapp

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class MapsFragment2 : Fragment() {
    var getLat : Double = 0.0
    var getLng : Double = 0.0
    var mData: DatabaseReference
    init {
        mData = FirebaseDatabase.getInstance().reference
    }

    private val callback = OnMapReadyCallback { googleMap ->

        val Hanoi = LatLng(getLat, getLng)
        googleMap.addMarker(MarkerOptions().position(Hanoi).title("My location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(Hanoi))
                val zoomLevel = 10f
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Hanoi, zoomLevel))


        mData.child("myLocation").child("lat").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var lat : String = snapshot.getValue().toString()
                getLat = lat.toDouble()

                mData.child("myLocation").child("lng").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var lng: String = snapshot.getValue().toString()
                        getLng = lng.toDouble()

                        val Hanoi = LatLng(getLat, getLng)
                        googleMap.clear()
                        googleMap.addMarker(MarkerOptions().position(Hanoi).title("My location"))
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(Hanoi))
                        val zoomLevel = 15f
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Hanoi, zoomLevel))
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_maps2, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}