package server;

public class Message {

  public enum Type {
    NONE,
    CLIENTIDS,
    MSGFROMCLIENT,
    MSGFROMSERVER,
    TOCLIENT,
    PLAYERNAME,
    PLAYERINFO,
    ATTACK,
    MAPDATA  // yeni satır
}


    public static String GenerateMsg(Message.Type type, String data) {
        return type + "#" + data;
    }
}
