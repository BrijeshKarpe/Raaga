package com.example.raaga.landing

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.raaga.data.Raga

@BindingAdapter("ragaName")
fun TextView.setRagaName(item:Raga){

    item.let {

        text = (item.name)!!.capitalize()
    }
}
@BindingAdapter("thaatName")
fun TextView.setThaatName(item:Raga){

    item.let {

        text = (item.thaat)!!.capitalize()
    }
}
@BindingAdapter("ragaNumber")
fun TextView.setRagaNumber(item:Raga){

    item.let {

        text = item.id
    }
}
@BindingAdapter("playButton")
fun ImageView.setPlayButton(item:Raga){

    item.let{
        //(int)typeof(Resource.Drawable).GetField("icon").GetValue(null)
        setImageResource(resources.getIdentifier(item.playButton, "drawable",context.packageName))
    }
}
