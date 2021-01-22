package com.example.raaga.prahars

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.raaga.data.Samay

@BindingAdapter("samayName")
fun TextView.setSamayName(item:Samay){

    item.let {

        text = (item.name).capitalize()
    }
}
@BindingAdapter("fromTime")
fun TextView.setFromTime(item:Samay){

    item.let {

        text = item.time_from
    }
}
@BindingAdapter("toTime")
fun TextView.setToTime(item:Samay){

    item.let {

        text = item.time_to
    }
}
@BindingAdapter("samayNumber")
fun TextView.setSamayNumber(item:Samay){

    item.let {

        text = item.id
    }
}