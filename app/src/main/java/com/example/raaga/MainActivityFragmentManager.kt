package com.example.raaga


import androidx.fragment.app.Fragment
import java.util.*


class MainActivityFragmentManager {
    private val mFragments: ArrayList<Fragment> = ArrayList<Fragment>()
    fun addFragment(fragment: Fragment) {
        mFragments.add(fragment)
    }

    fun removeFragment(fragment: Fragment?) {
        mFragments.remove(fragment)
    }

    fun removeFragment(position: Int) {
        mFragments.removeAt(position)
    }

    val fragments: ArrayList<Fragment>
        get() = mFragments

    fun removeAllFragments() {
        mFragments.clear()
    }

    companion object {
        var instance: MainActivityFragmentManager? = null
            get() {
                if (field == null) {
                    field = MainActivityFragmentManager()
                }
                return field
            }
            private set
    }
}