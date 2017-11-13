package com.example.raj.echo.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.example.raj.echo.R


/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {
    var myActivity:Activity?=null
    var shakeSwitch:Switch?=null
    object Statified{

        var PREFS_NAME="ShakeFeature"
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
       var view = inflater!!.inflate(R.layout.fragment_settings, container, false)
        shakeSwitch=view?.findViewById(R.id.switchShake)
        setHasOptionsMenu(true)
        activity.title = "Settings"
        // Inflate the layout for this fragment
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity=context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val prefs = myActivity?.getSharedPreferences(Statified.PREFS_NAME,Context.MODE_PRIVATE)
        val isAllowed = prefs?.getBoolean("feature",false)
        if(isAllowed as Boolean){
            shakeSwitch?.isChecked=true
        }else{
            shakeSwitch?.isChecked=false
        }
        shakeSwitch?.setOnCheckedChangeListener({compoundButton,b ->
            if(b){
                val editor=myActivity?.getSharedPreferences(Statified.PREFS_NAME,Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature",true)
                editor?.apply()
            }else{
                val editor=myActivity?.getSharedPreferences(Statified.PREFS_NAME,Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature",false)
                editor?.apply()
            }
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.action_sort)
        item?.isVisible= false
    }

}// Required empty public constructor
