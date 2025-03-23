# Chat 1.0

**Curso:** Sistemas Distribuidos 2024-2025  
**Autor:** Elisa Rodríguez Domínguez  
**Versión:** 4.0  

Este proyecto implementa un sistema de chat basado en sockets TCP. El objetivo es permitir la comunicación remota entre varios clientes a través de un servidor central que retransmite los mensajes. Además, incluye características adicionales como bloquear y desbloquear usuarios.

---

## 📋 Descripción del Proyecto

### Funcionalidades principales:
1. **Conexión de usuarios al servidor:** Los usuarios se registran con un apodo único (nickname).
2. **Intercambio de mensajes:** Los mensajes enviados por un usuario se retransmiten a todos los clientes conectados.
3. **Bloqueo y desbloqueo de usuarios:**
   - Los usuarios pueden bloquear mensajes de otros usuarios mediante el comando `ban <usuario>`.
   - Los mensajes de los usuarios bloqueados no se mostrarán hasta que se utilice el comando `unban <usuario>`.
4. **Comando de logout:** Los usuarios pueden salir del chat utilizando el comando `/logout`.

### Características Técnicas:
- **Comunicación en red:** Uso de sockets Java (TCP) para una comunicación orientada a conexión.
- **Serialización de mensajes:** Los mensajes se transmiten como objetos serializados mediante `ObjectInputStream` y `ObjectOutputStream`.
- **Modelo de servidor push:** El servidor mantiene un registro de los clientes conectados y retransmite los mensajes.

---

## COMENTARIOS.
Uso el puerto  1600 en vez de el 1500 ya que lo tengo en uso y no queria detener esa ejecución y en cuanto a la ejecución del proyecto, no he seguido la estructura de clases que 
indica la practica, asi que yo ejecuto el servidor a traves de ChatServer y inicio los clientes a traves de la clase ChatClient, espero que no cause ningun inconveniente.

