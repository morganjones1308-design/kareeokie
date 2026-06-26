package com.careeokie

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class CarService : CarAppService() {
    // Allow all hosts during development. For Play Store release, restrict this
    // to the official Android Auto host certificate.
    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = CareeokeSession()
}
