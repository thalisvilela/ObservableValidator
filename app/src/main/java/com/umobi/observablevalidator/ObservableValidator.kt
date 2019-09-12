package com.umobi.observablevalidator

import android.util.Log
import androidx.collection.ArrayMap
import androidx.databinding.Observable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.ArrayList

class ObservableValidator <T:Observable>(private val liveData:LiveData<T>, private val bindingResource:Class<*>) {

    val  TAG:String = "ObservableValidator"

    private val rules = ArrayMap<Int, ArrayList<ValidationRule>>()

    private val validations = ArrayMap<Int, MutableLiveData<String>>()

    fun getValidation(property: String): LiveData<String>?{
        var field: Field = bindingResource.getDeclaredField(property)
        field.isAccessible = true
        var genBr:Int = field.getInt(null)
        return validations[genBr]
    }

    init {
        liveData.value?.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                logRules(propertyId)
                validateField(propertyId)
            }
        })
    }

    fun validateAll():Boolean{
        rules.entries.forEach{
            it.value.forEach {
                validateField(it.propertyId)
            }
        }
        var response = true

        validations.entries.forEach {
            if(!it.value?.value.isNullOrEmpty()) response = false
        }

        return response
    }

    fun validateField(propertyId: Int){
        rules[propertyId]?.forEach {
            logProperty(it.property)
            if(!validateProperty(it)) return //return if validation hit a invalid state
        }
    }

    private fun validateProperty(validationRule:ValidationRule):Boolean{
        when(validationRule.rule){
            ValidationRules.FIELD_REQUIRED -> {
                return validateRequired(validationRule)
            }
            ValidationRules.FIELD_EMAIL -> {
                return validateEmail(validationRule)
            }
            else -> {

            }
        }
        return true
    }

    fun addRule(property:String, rule:ValidationRules, message:String, otherProperty: String? = null){
        var field: Field = bindingResource.getDeclaredField(property)
        field.isAccessible = true
        var genBr:Int = field.getInt(null)
        Log.d(TAG,"Generic bindingResource: $genBr")
        Log.d(TAG, "addRule(property: $property, rule:$rule, message: $message)")

        validations[genBr] = MutableLiveData()

        if(rules[genBr]==null)rules[genBr] = ArrayList()
        rules[genBr]?.add(ValidationRule(genBr,rule,property,message, otherProperty))

        Collections.sort(rules[genBr], ValidationRulesComparator())
    }

    fun logRules(propertyId: Int){
        rules[propertyId]?.forEach {
            logProperty(it.property)
        }
    }

    private fun logProperty(property:String){
        try{
            var value:String = liveData.value!!::class.java.getMethod("get" + property.capitalize()).invoke(liveData.value).toString()
            Log.d(TAG, value)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun getProperty(property:String):Any{
        return liveData.value!!::class.java.getMethod("get" + property.capitalize()).invoke(liveData.value)
    }

    private fun validateRequired(validationRule: ValidationRule):Boolean{
        var value = getProperty(validationRule.property)
        if(value is Int){

        }else if(value is String){
            if(value.isNullOrEmpty()){
                validations[validationRule.propertyId].also {
                    it?.value = validationRule.message
                }
                return false
            }
        }

        validations[validationRule.propertyId].also {
            it?.value = null
        }

        return true

    }

    private fun validateEmail(validationRule: ValidationRule):Boolean{
        var value = getProperty(validationRule.property)

        if(value is String){
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()){
                validations[validationRule.propertyId].also {
                    it?.value = validationRule.message
                }
                return false
            }
        }

        validations[validationRule.propertyId].also {
            it?.value = null
        }
        return true

    }

    data class ValidationRule(
        var propertyId:Int,
        var rule:ValidationRules,
        var property:String,
        var message:String,
        var otherProperty:String? = null
    )

    internal inner class ValidationRulesComparator : Comparator<ValidationRule> {
        override fun compare(left: ValidationRule, right: ValidationRule): Int {
            return left.rule.compareTo(right.rule)
        }
    }

    enum class ValidationRules{
        FIELD_REQUIRED,
        FIELD_TOO_SHORT,
        FIELD_TOO_LONG,
        FIELD_EMAIL,
        FIELD_NOT_MATCH
    }

}