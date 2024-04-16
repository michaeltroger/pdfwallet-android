package com.michaeltroger.gruenerpass.mapper

import com.michaeltroger.gruenerpass.db.Certificate
import com.michaeltroger.gruenerpass.pdfimporter.PendingCertificate

fun PendingCertificate.toCertificate(): Certificate {
    return Certificate(
        id = fileName,
        name = documentName,
    )
}