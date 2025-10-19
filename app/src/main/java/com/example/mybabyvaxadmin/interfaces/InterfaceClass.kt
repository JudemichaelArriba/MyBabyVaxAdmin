package com.example.iptfinal.interfaces


import com.example.mybabyvaxadmin.models.Baby
import com.example.mybabyvaxadmin.models.MergedSchedule
import com.example.mybabyvaxadmin.models.Users

interface InterfaceClass {


    interface PurokCallback {
        fun onPuroksLoaded(puroks: List<String>)
        fun onError(message: String)
    }

    interface StatusCallback {
        fun onSuccess(message: String)
        fun onError(message: String)
    }

    interface UserCallback {
        fun onUserLoaded(user: Users)
        fun onError(message: String)
    }

    interface StatusCallbackWithId {
        fun onSuccess(message: String, id: String)
        fun onFailure(message: String)
    }

    interface MergedScheduleCallback {
        fun onMergedSchedulesLoaded(schedules: List<MergedSchedule>)
        fun onError(error: String)
    }

    interface BabyCallback {
        fun onBabyLoaded(baby: Baby)
        fun onError(message: String)
    }

}
