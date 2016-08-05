java-chat
=========

This training project for a mipt java course is a simple multi-client chat-room with GUI. 

A host can create server in a local network, a client with a unique nickname can connect to the chat-room knowing the host IP address. Server notifies clients about connections and disconnections of clients. When client connects to room, server replies with last 10 chat messages. It is multi-threading project, every client handled by separated auxiliary server thread. MVP concept is implemented.

## Running

To host chat-room run Server.jar

To connect to any server run  Client.jar


