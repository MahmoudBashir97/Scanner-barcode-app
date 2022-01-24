package com.mahmoudbashir.thirdwayvtaskchallange.dataLocal

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun <T> LiveData<T>.getOrAwaitValue(
    time:Long = 2,
    timeUnit:TimeUnit = TimeUnit.SECONDS,
    afterObserve:()-> Unit ={}): T? {
    var data: T? = null
    var latch = CountDownLatch(1)
    var observer = object : Observer<T>{
        override fun onChanged(t: T) {
            data = t
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)
    try {
        afterObserve.invoke()

    }finally {

    }
    return data
}