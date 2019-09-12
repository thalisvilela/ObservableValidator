package com.umobi.observablevalidatorsample.data.model

import androidx.databinding.InverseMethod

class Converters {

    companion object{
        @JvmStatic
        @InverseMethod("stringToInt")
        fun intToString(int:Int?):String?{
            if(int==null) return ""

            return int.toString()
        }
        @JvmStatic
        fun stringToInt(string:String?):Int?{
            if(string.isNullOrEmpty()) return null
            return string.toInt()
        }
    }

}