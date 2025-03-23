package es.ubu.lsi.common;

import java.io.Serializable;

/**
 * La enumeración MessageType representa los diferentes tipos de mensajes
 * que pueden ser intercambiados en un sistema de chat. Implementa la 
 * interfaz {@link Serializable} para permitir su transmisión a través 
 * de streams.
 */
public enum MessageType implements Serializable {
    
    /**
     * Representa un mensaje de conexión que indica que un cliente 
     * se ha conectado al sistema.
     */
    CONNECT,   
    
    /**
     * Representa un mensaje de desconexión que indica que un cliente 
     * se ha desconectado del sistema.
     */
    DISCONNECT, 
    
    /**
     * Representa un mensaje normal enviado por un cliente.
     */
    MESSAGE,    
    
    /**
     * Representa un comando para bloquear (banear) a otro usuario.
     */
    BAN,        
    
    /**
     * Representa un comando para desbloquear a un usuario previamente baneado.
     */
    UNBAN       
}
