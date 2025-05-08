package server;

public class Message {

    public enum Type {
        NONE,
        CLIENTIDS,
        MSGFROMCLIENT,
        MSGFROMSERVER,
        TOCLIENT
    }

    public static String GenerateMsg(Message.Type type, String data) {
        return type + "#" + data;
    }
}
