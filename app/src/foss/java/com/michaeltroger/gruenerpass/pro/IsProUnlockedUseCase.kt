package com.michaeltroger.gruenerpass.pro

import javax.inject.Inject

class IsProUnlockedUseCase @Inject constructor() {
    operator fun invoke(): Boolean {
        return true
    }
}
