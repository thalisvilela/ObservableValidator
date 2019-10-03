package com.umobi.observablevalidatorsample.data.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
class RegisterUser:BaseObservable(){

    @Bindable
    var name:String?=""
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }

    @Bindable
    var email:String?=""
        set(value) {
            field = value
            notifyPropertyChanged(BR.email)
        }


    @Bindable
    var age:Int?=null
        set(value) {
            field = value
            notifyPropertyChanged(BR.age)
        }

    @Bindable
    var reminder:String?=null
        set(value) {
            field = value
            notifyPropertyChanged(BR.reminder)
        }

    @Bindable
    var password:String?=""
        set(value) {
            field = value
            notifyPropertyChanged(BR.password)
        }

    @Bindable
    var passwordConfirmation:String?=""
        set(value) {
            field = value
            notifyPropertyChanged(BR.passwordConfirmation)
        }

    @Bindable
    var acceptTerms:Boolean?=false
        set(value) {
            field = value
            notifyPropertyChanged(BR.acceptTerms)
        }
}
