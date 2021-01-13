public interface JsonWrapper  {
    boolean isArray();
    boolean isBoolean();
    boolean isDouble();
    boolean isInt();
    boolean isLong();
    boolean isObject();
    boolean isString();

    boolean  getBoolean ();
    double   getDouble ();
    int      getInt ();
    JsonType getJsonType ();
    long     getLong ();
    String   getString ();

    void setBoolean  (boolean val);
    void setDouble   (double val);
    void setInt      (int val);
    void setJsonType (JsonType type);
    void setLong     (long val);
    void setString   (String val);

    String toJson ();
    void   toJson (JsonWriter writer);
}
