package com.example.raaga.raagas

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.raaga.R
import com.example.raaga.data.Raga

@BindingAdapter("ragaName1")
fun TextView.setRagaName1(item: Raga){

    item.let {

        text = (item.name).capitalize()
    }
}
@BindingAdapter("thaatName1")
fun TextView.setThaatName1(item: Raga){

    item.let {

        text = (item.thaat).capitalize()
    }
}
@BindingAdapter("ragaNumber1")
fun TextView.setRagaNumber1(item: Raga){

    item.let {

        text = item.id
    }
}
@BindingAdapter("playButton1")
fun ImageView.setPlayButton1(item: Raga){

    item.let{

        setImageResource(R.drawable.ic_media_play)
    }
}
