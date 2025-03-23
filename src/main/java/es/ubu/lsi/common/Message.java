package es.ubu.lsi.common;

import java.io.Serializable;

/**
 * La clase Message representa un mensaje en el sistema de chat,
 * que incluye su tipo, el remitente y el contenido.
 * Implementa la interfaz {@link Serializable} para permitir la
 * transmisión de objetos a través de streams.
 */
public class Message implements Serializable {
    
    /**
     * Identificador único para la serialización.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Tipo de mensaje (CONNECT, DISCONNECT, MESSAGE, BAN, UNBAN).
     */
    private MessageType type;

    /**
     * Nombre del usuario que envía el mensaje.
     */
    private String sender;

    /**
     * Contenido del mensaje.
     */
    private String content;

    /**
     * Constructor de la clase Message.
     * 
     * @param type El tipo de mensaje (de tipo {@link MessageType}).
     * @param sender El nombre del remitente.
     * @param content El contenido del mensaje.
     */
    public Message(MessageType type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
    }

    /**
     * Devuelve el tipo del mensaje.
     * 
     * @return El tipo de mensaje ({@link MessageType}).
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Devuelve el nombre del remitente del mensaje.
     * 
     * @return El nombre del remitente.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Devuelve el contenido del mensaje.
     * 
     * @return El contenido del mensaje.
     */
    public String getContent() {
        return content;
    }

    /**
     * Devuelve una representación en forma de cadena del mensaje.
     * 
     * @return Una cadena con el formato: "[tipo] remitente: contenido".
     */
    @Override
    public String toString() {
        return "[" + type + "] " + sender + ": " + content;
    }
}
