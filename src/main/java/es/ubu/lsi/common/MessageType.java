package es.ubu.lsi.common;

import java.io.Serializable;

public enum MessageType implements Serializable {
    CONNECT,   // Un cliente se conecta
    DISCONNECT, // Un cliente se desconecta
    MESSAGE,    // Mensaje normal
    BAN,        // Bloquear a un usuario
    UNBAN       // Desbloquear a un usuario
}
