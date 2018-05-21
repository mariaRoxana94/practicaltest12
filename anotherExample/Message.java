package ro.pub.cs.systems.eim.practicaltest02;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;



public class Message implements Serializable
{
    String key;
    String value;
    int operation; // 0 - get / 1 - put

    public Message(String key, String value, int operation)
    {
        this.key = key;
        this.value = value;
        this.operation = operation;
    }

    public static Message fromString(String s) throws IOException, ClassNotFoundException
    {
        byte[] data = Base64Coder.decode(s);
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data));

        Message m = (Message)objectInputStream.readObject();
        objectInputStream.close();

        return m;
    }

    public static String toString(Message m) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(m);
        objectOutputStream.close();

        return new String(Base64Coder.encode(byteArrayOutputStream.toByteArray()));
    }
}
