package com.example.carwash.util

fun Throwable.toUserMessage(defaultMessage: String): String {
    val rawMessage = message.orEmpty()
    val normalized = rawMessage.lowercase()

    return when {
        "invalid login credentials" in normalized || "invalid_grant" in normalized ->
            "Correo o contrasena incorrectos."
        "email not confirmed" in normalized ->
            "Tu correo aun no ha sido confirmado."
        "user not found" in normalized ->
            "No se encontro una cuenta con ese correo."
        "too many requests" in normalized || "rate limit" in normalized ->
            "Demasiados intentos. Espera un momento e intenta de nuevo."
        "la sesion aun se esta sincronizando" in normalized || "session" in normalized && "sync" in normalized ->
            "Estamos terminando de sincronizar tu sesion. Intenta de nuevo en unos segundos."
        "time expired" in normalized || "timeout" in normalized || "timed out" in normalized || "10000ms" in normalized ->
            "La solicitud tardo demasiado. Verifica tu conexion e intenta de nuevo."
        "unable to resolve host" in normalized ||
            "failed to connect" in normalized ||
            "network is unreachable" in normalized ||
            "connection reset" in normalized ||
            "failed to connect to" in normalized ||
            "socket closed" in normalized ||
            "network" in normalized ->
            "Sin conexion a internet. Verifica tu red e intenta de nuevo."
        "jwt expired" in normalized || "invalid jwt" in normalized || "unauthorized" in normalized ->
            "Tu sesion expiro. Vuelve a iniciar sesion."
        "forbidden" in normalized || "permission denied" in normalized || "row-level security" in normalized ->
            "No tienes permisos para realizar esta accion."
        "duplicate key" in normalized || "unique constraint" in normalized || "already exists" in normalized ->
            "Ese registro ya existe."
        "foreign key" in normalized ->
            "No se pudo completar la operacion por datos relacionados."
        else -> defaultMessage
    }
}
