package com.umobi.observablevalidator

data class ValidationRule(
    val propertyId:Int,
    val rule: ValidationFlags,
    val property:String,
    val message:String,
    val otherProperty:String? = null,
    val limit:Int? = null
)
