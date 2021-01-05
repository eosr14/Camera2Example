package com.example.finiview.camera.common.event

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object RxEventBus {
    private val subject = PublishSubject.create<EventBusInterface>()

    fun sendEvent(event: EventBusInterface) {
        subject.onNext(event)
    }

    fun getEvent() : Observable<EventBusInterface> {
        return subject
    }

    fun <T> getSingleEventType(eventType: Class<T>): Observable<T> = subject.ofType(eventType)
}