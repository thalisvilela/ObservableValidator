package com.umobi.observablevalidatorsample.ui.login

import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.umobi.observablevalidator.ObservableValidator
import com.umobi.observablevalidator.ValidationFlags
import com.umobi.observablevalidatorsample.data.model.RegisterUser


class RegisterViewModel : ViewModel() {

    var user:LiveData<RegisterUser> = MutableLiveData<RegisterUser>().also {
        it.value = RegisterUser()
    }

    var validator = ObservableValidator(user.value!!, BR::class.java).apply {
        addRule("name", ValidationFlags.FIELD_REQUIRED, "Enter your name")

        addRule("email", ValidationFlags.FIELD_REQUIRED, "Enter your email")
        addRule("email", ValidationFlags.FIELD_EMAIL, "Enter a valid email")

        addRule("age", ValidationFlags.FIELD_REQUIRED, "Enter your age (Underage or too old?)")
        addRule("age", ValidationFlags.FIELD_MIN, "You can't be underage!", limit = 18)
        addRule("age", ValidationFlags.FIELD_MAX, "You sure you're still alive?", limit = 100)

        addRule("password", ValidationFlags.FIELD_REQUIRED, "Enter your password")

        addRule("passwordConfirmation", ValidationFlags.FIELD_REQUIRED, "Enter password confirmation")
        addRule("passwordConfirmation", ValidationFlags.FIELD_MATCH, "Passwords don't match", "password")
    }

}
