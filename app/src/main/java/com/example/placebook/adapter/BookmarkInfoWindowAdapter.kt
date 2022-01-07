package com.example.placebook.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import com.example.placebook.databinding.ContentBookmarkInfoBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker


class BookmarkInfoWindowAdapter(context: Activity) : GoogleMap.InfoWindowAdapter {
    // 3
    private val binding =
        ContentBookmarkInfoBinding.inflate(context.layoutInflater)

    // 4
    override fun getInfoWindow(marker: Marker): View? {
// This function is required, but can return null if
// not replacing the entire info window
        return null
    }

    // 5
    override fun getInfoContents(marker: Marker): View? {
        val imageView=binding.photo
        binding.title.text = marker.title ?: ""
        binding.phone.text = marker.snippet ?: ""
        imageView?.setImageBitmap(marker.tag as? Bitmap)
        return binding.root
    }
}
