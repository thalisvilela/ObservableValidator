package com.umobi.observablevalidator

import android.util.Log
import androidx.collection.ArrayMap
import androidx.databinding.Observable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.ArrayList

class ObservableValidator <T:Observable>(private val observedObject:T, private val bindingResource:Class<*>) {

    val TAG:String = "ObservableValidator"

    //Store the validation rules at a ValidationRule List, by fieldId
    private val rules = ArrayMap<Int, ArrayList<ValidationRule>>()

    //Store the field error string, or null if validation pass
    private val validations = ArrayMap<Int, MutableLiveData<String>>()

    //Get the LiveData<String> for the property
    fun getValidation(property: String): LiveData<String>?{
        val field: Field = bindingResource.getDeclaredField(property)
        field.isAccessible = true
        val genBr:Int = field.getInt(null)
        return validations[genBr]
    }


    //Add the callback to the observedObject
    init {
        observedObject.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                logRules(propertyId)
                validateField(propertyId)
            }
        })
    }


    //Validate all fields
    fun validateAll():Boolean{

        //Run validation for all fields
        rules.entries.forEach{
            it.value.forEach {
                validateField(it.propertyId)
            }
        }

        //Default response is valid=true
        var response = true

        //If there is any string message set, some validation must have failed, so response is invalid=false
        validations.entries.forEach {
            if(!it.value?.value.isNullOrEmpty()) response = false
        }

        //the response
        return response
    }

    fun clearAllErrors(){
        validations.entries.forEach {
            it.value?.value = null
        }
    }

    //Run all validations for this field, stop if we fail some rule
    fun validateField(propertyId: Int){
        rules[propertyId]?.forEach {
            logProperty(it.property)
            if(!validateProperty(it)) return //return if validation hit a invalid state
        }
    }

    //Run the proper validation function for the rule
    private fun validateProperty(validationRule:ValidationRule):Boolean{
        when(validationRule.rule){
            ValidationFlags.FIELD_REQUIRED -> {
                return validateRequired(validationRule)
            }
            ValidationFlags.FIELD_EMAIL -> {
                return validateEmail(validationRule)
            }
            ValidationFlags.FIELD_MATCH -> {
                return validateMatch(validationRule)
            }
            ValidationFlags.FIELD_MAX -> {
                return validateMax(validationRule)
            }
            ValidationFlags.FIELD_MIN -> {
                return validateMin(validationRule)
            }

        }
    }


    //Add a validation rule
    fun addRule(field:String, rule: ValidationFlags, message:String, otherField: String? = null, limit: Int? = null){

        //Get the field BR Int
        val bindingField: Field = bindingResource.getDeclaredField(field)
        bindingField.isAccessible = true
        val propertyId:Int = bindingField.getInt(null)

        Log.d(TAG,"Generic bindingResource: $propertyId")
        Log.d(TAG, "addRule(field: $field, rule:$rule, message: $message), other field: $otherField, limit: $limit")

        //Initialize the LiveData<String> for this field
        validations[propertyId] = MutableLiveData()

        //If there's no rule yet, initialize the rule array
        if(rules[propertyId]==null)rules[propertyId] = ArrayList()

        //Add this rule to the array
        rules[propertyId]?.add(ValidationRule(propertyId,rule,field,message, otherField, limit))

        //Order the rules so we run FIELD_REQUIRED first, and others after
        Collections.sort(rules[propertyId], ValidationRulesComparator())
    }

    //Yep logszz
    fun logRules(propertyId: Int){
        rules[propertyId]?.forEach {
            logProperty(it.property)
        }
    }

    //Yep... logggin
    private fun logProperty(property:String){
        try{
            var value:String = observedObject::class.java.getMethod("get" + property.capitalize()).invoke(observedObject).toString()
            //Log.d(TAG, value)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    //Get the actual value of the property using the field getter: get+FieldName
    private fun getProperty(property:String):Any?{
        return observedObject::class.java.getMethod("get" + property.capitalize()).invoke(observedObject)
    }

    //Validate rule FIELD_MAX
    private fun validateMax(validationRule: ValidationRule):Boolean{
        var value = getProperty(validationRule.property)

        if(validationRule.limit!=null){
            if(value is Int){
                if(value > validationRule.limit){
                    validations[validationRule.propertyId].also {
                        it?.value = validationRule.message
                    }
                    return false
                }
            }else if(value is String){
                if(value.length > validationRule.limit){
                    validations[validationRule.propertyId].also {
                        it?.value = validationRule.message
                    }
                    return false
                }
            }
        }

        validations[validationRule.propertyId].also {
            it?.value = null
        }

        return true

    }

    //Validate rule FIELD_MIN
    private fun validateMin(validationRule: ValidationRule):Boolean{
        var value = getProperty(validationRule.property)

        if(validationRule.limit!=null){
            if(value is Int){
                if(value < validationRule.limit){
                    validations[validationRule.propertyId].also {
                        it?.value = validationRule.message
                    }
                    return false
                }
            }else if(value is String){
                if(value.length < validationRule.limit){
                    validations[validationRule.propertyId].also {
                        it?.value = validationRule.message
                    }
                    return false
                }
            }
        }

        validations[validationRule.propertyId].also {
            it?.value = null
        }

        return true

    }

    //Validate rule FIELD_REQUIRED
    private fun validateRequired(validationRule: ValidationRule):Boolean{

        var value = getProperty(validationRule.property)

        if(value==null){
            validations[validationRule.propertyId].also {
                it?.value = validationRule.message
            }
            return false
        }
        if(value is Boolean){
            if(!value){
                validations[validationRule.propertyId].also {
                    it?.value = validationRule.message
                }
                return false
            }
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

    //Validate rule FIELD_MATCH
    private fun validateMatch(validationRule: ValidationRule):Boolean{
        var value1 = getProperty(validationRule.property)
        if(validationRule.otherProperty!=null){
            var value2 = getProperty(validationRule.otherProperty)

            if(value1 is Int && value2 is Int){
                if(value1!=value2){
                    validations[validationRule.propertyId].also {
                        it?.value = validationRule.message
                    }
                    return false
                }
            }else if(value1 is String && value2 is String){
                if(value1!=value2){
                    validations[validationRule.propertyId].also {
                        it?.value = validationRule.message
                    }
                    return false
                }
            }

        }else{
            validations[validationRule.propertyId].also {
                it?.value = validationRule.message
            }
            return false
        }

        validations[validationRule.propertyId].also {
            it?.value = null
        }
        return true

    }

    //Validate rule FIELD_EMAIL
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


    internal inner class ValidationRulesComparator : Comparator<ValidationRule> {
        override fun compare(left: ValidationRule, right: ValidationRule): Int {
            return left.rule.compareTo(right.rule)
        }
    }

}