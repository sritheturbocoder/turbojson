import exception.ArgumentNullException;
import exception.JsonException;

import java.io.IOException;
import java.lang.reflect.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;

public class JsonMapper {

    private static int max_nesting_depth;

    private static Map<Type, BiFunction<Object, JsonWriter, Integer>> base_exporters_table;
    private static Map<Type, BiFunction<Object, JsonWriter, Integer>> custom_exporters_table;

    private static Map<Type, Map<Type, Function<Object, Object>>> base_importers_table;
    private static Map<Type,
            Map<Type, Function<Object, Object>>> custom_importers_table;

    private static Map<Type, ArrayMetadata> array_metadata;
    private static Object array_metadata_lock = new Object();

    private static HashMap<Type, HashMap<Type, Method>> conv_ops;
    private static Object conv_ops_lock = new Object();

    private static Map<Type, ObjectMetadata> object_metadata;
    private static Object object_metadata_lock = new Object();

    private static Map<Type,
            List<FieldMethodMetadata>> type_properties;
    private static Object type_properties_lock = new Object();

    private static JsonWriter static_writer;
    private static Object static_writer_lock = new Object();

    JsonMapper() throws ArgumentNullException {
        max_nesting_depth = 100;

        array_metadata = new HashMap<>();
        conv_ops = new HashMap<>();
        object_metadata = new HashMap<>();
        type_properties = new HashMap<>();

        static_writer = new JsonWriter();

        base_exporters_table = new HashMap<>();
        custom_exporters_table = new HashMap<>();

        base_importers_table = new HashMap<>();
        custom_importers_table = new HashMap<>();

        registerBaseExporters();
        registerBaseImporters();
    }

    private void registerBaseExporters() {
        base_exporters_table.put(byte.class, (Object obj, JsonWriter writer) -> {
            try {
                writer.Write((int) obj);
                return Constants.SUCCESS;
            } catch (JsonException e) {
                e.printStackTrace();
                return Constants.FAILURE;
            }
        });

        base_exporters_table.put(char.class, (Object obj, JsonWriter writer) -> {
            try {
                writer.Write((char) obj);
                return Constants.SUCCESS;
            } catch (JsonException e) {
                e.printStackTrace();
                return Constants.FAILURE;
            }
        });

        base_exporters_table.put(LocalDateTime.class, (Object obj, JsonWriter writer) -> {
            try {
                LocalDateTime dateTime = (LocalDateTime) obj;
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
                String dateTimeString = dateFormat.format(dateTime);
                writer.Write(dateTimeString);
                return Constants.SUCCESS;
            } catch (JsonException e) {
                e.printStackTrace();
                return Constants.FAILURE;
            }
        });

        base_exporters_table.put(Boolean.class, (Object obj, JsonWriter writer) -> {
            try {
                writer.Write((boolean) obj);
                return Constants.SUCCESS;
            } catch (JsonException e) {
                e.printStackTrace();
                return Constants.FAILURE;
            }
        });

        base_exporters_table.put(Double.class, (Object obj, JsonWriter writer) -> {
            try {
                writer.Write((double) obj);
                return Constants.SUCCESS;
            } catch (JsonException e) {
                e.printStackTrace();
                return Constants.FAILURE;
            }
        });

        base_exporters_table.put(Integer.class, (Object obj, JsonWriter writer) -> {
            try {
                writer.Write((Integer) obj);
                return Constants.SUCCESS;
            } catch (JsonException e) {
                e.printStackTrace();
                return Constants.FAILURE;
            }
        });

        base_exporters_table.put(long.class, (Object obj, JsonWriter writer) -> {
            try {
                writer.Write((long) obj);
                return Constants.SUCCESS;
            } catch (JsonException e) {
                e.printStackTrace();
                return Constants.FAILURE;
            }
        });

        base_exporters_table.put(String.class, (Object obj, JsonWriter writer) -> {
            try {
                writer.Write((String) obj);
                return Constants.SUCCESS;
            } catch (JsonException e) {
                e.printStackTrace();
                return Constants.FAILURE;
            }
        });
    }

    private void registerBaseImporters() {
        registerImporter(base_importers_table, Integer.class, Byte.class, (Object input) -> input);
        registerImporter(base_importers_table, Integer.class, Integer.class, (Object input) -> input);
        registerImporter(base_importers_table, Integer.class, Long.class, (Object input) -> input);
        registerImporter(base_importers_table, Integer.class, Short.class, (Object input) -> input);
        registerImporter(base_importers_table, Integer.class, Double.class, (Object input) -> input);
        registerImporter(base_importers_table, String.class, char.class, (Object input) -> input);
        registerImporter(base_importers_table, String.class, LocalDateTime.class, (Object input) -> input);
    }

    private static void registerImporter(
            Map<Type, Map<Type, Function<Object, Object>>> table,
            Type json_type, Type value_type, Function<Object, Object> importer) {
        if (!table.containsKey(json_type))
            table.put(json_type, new HashMap<>());

        table.get(json_type).put(value_type, importer);
    }

    private static void addArrayMetadata(Type type) {
        if (array_metadata.containsKey(type))
            return;

        ArrayMetadata data = new ArrayMetadata();

        data.setIs_array(type.getClass().isArray());

        if (Arrays.stream(type.getClass().getInterfaces()).anyMatch(inter -> inter.toString().contains("Collection"))) {
            data.setIs_list(true);
        }

        Method[] methods = type.getClass().getMethods();

        Arrays.stream(methods).filter(method -> method.getName().equals("get"))
                              .filter(method -> Modifier.isPublic(method.getModifiers()))
                              .filter(method -> method.getParameterCount() > 0)
                              .filter(method -> method.getParameters()[0].getClass().toString().equals(int.class.toString())).findFirst()
                              .ifPresent(method -> data.setElement_type(method.getParameters()[0].getClass()));
    }

    private static void addObjectMetadata(Type type)
    {
        if (object_metadata.containsKey (type))
            return;

        ObjectMetadata data = new ObjectMetadata ();

        if (Arrays.stream(type.getClass().getInterfaces()).anyMatch(inter -> inter.toString().contains("Dictionary"))) {
            data.setIs_dictionary(true);
        }

        data.setProperties(new HashMap<> ());

        Method[] methods = type.getClass().getMethods();
        Arrays.stream(methods).filter(method -> method.getName()
                              .equals("get"))
                              .filter(method -> method.getParameterCount() > 0)
                              .filter(method -> Modifier.isPublic(method.getModifiers()))
                              .filter(method -> method.getParameters()[0].getClass().toString().equals(String.class.toString()))
                              .findFirst().ifPresent(method -> data.setElement_type(method.getParameters()[0].getClass()));

        for (Field f_info : type.getClass().getFields()) {
            FieldMethodMetadata p_data = new FieldMethodMetadata();
            p_data.info = f_info;
            p_data.isField = true;
            p_data.type = f_info.getType();
            data.fieldAndMethodsMetaData().put(f_info.getName(), p_data);

            FieldMethodMetadata getMetadata = new FieldMethodMetadata();
            Arrays.stream(methods).filter(method -> method.getName().toLowerCase(Locale.ROOT).equals("get" + f_info.getName().toLowerCase(Locale.ROOT)))
                                  .filter(method -> Modifier.isPublic(method.getModifiers()))
                                  .findFirst().ifPresent(method -> {
                                                                    getMetadata.mInfo = method;
                                                                    getMetadata.type = method.getReturnType();
                                                                   });
            data.fieldAndMethodsMetaData().put(getMetadata.info.getName(),getMetadata);

            FieldMethodMetadata setMetadata = new FieldMethodMetadata();
            Arrays.stream(methods).filter(method -> method.getName().toLowerCase(Locale.ROOT)
                    .equals("set" + f_info.getName().toLowerCase(Locale.ROOT)))
                    .filter(method -> Modifier.isPublic(method.getModifiers()))
                    .findFirst().ifPresent(method -> {
                setMetadata.mInfo = method;
                setMetadata.type = method.getParameters()[0].getType();
            });
            data.fieldAndMethodsMetaData().put(setMetadata.info.getName(),setMetadata);
        }

        object_metadata.put (type, data);
    }

    private Object readValue (Type inst_type, JsonReader reader) throws InterruptedException, ExecutionException, JsonException, IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        reader.Read ();

        if (reader.getToken() == JsonToken.ArrayEnd)
            return null;

        if (reader.getToken() == JsonToken.Double ||
                reader.getToken() == JsonToken.Int ||
                reader.getToken() == JsonToken.Long ||
                reader.getToken() == JsonToken.String ||
                reader.getToken() == JsonToken.Boolean) {

            Type json_type = reader.getToken_value().getClass();

            if (inst_type.getClass().isAssignableFrom((Class<?>) json_type)) {
                return reader.getToken_value();
            }

            if (custom_importers_table.containsKey (json_type) &&
                    custom_importers_table.get(json_type).containsKey (inst_type)) {
                Function<Object, Object> importer = custom_importers_table.get(json_type).get(inst_type);
                return importer.apply(reader.getToken_value());
            }

            if (base_importers_table.containsKey (json_type) &&
                    base_importers_table.get(json_type).containsKey (inst_type)) {
                Function<Object, Object> importer = base_importers_table.get(json_type).get(inst_type);
                return importer.apply(reader.getToken_value());
            }


            throw new JsonException (String.format (
                    "Can't assign value %s (type %s) to type %s",
                    reader.getToken_value(), json_type, inst_type));
        }

        Object instance;

        if (reader.getToken() == JsonToken.ArrayStart) {

            addArrayMetadata (inst_type);
            ArrayMetadata t_data = array_metadata.get(inst_type);

            if (! t_data.isIs_array() && ! t_data.isIs_list())
                throw new JsonException (String.format(
                        "Type %s can't act as an array",
                        inst_type));

            List<Object> list = new ArrayList<>();
            Type elem_type = inst_type.getClass ();

            while (true) {
                Object item = readValue (elem_type, reader);
                if (item == null && reader.getToken() == JsonToken.ArrayEnd)
                    break;

                list.add (item);
            }

            if (t_data.isIs_array()) {
                int n = list.size();
                instance = Array.newInstance ((Class<?>) elem_type, n);

                for (int i = 0; i < n; i++)
                    ((ArrayList) instance).setValue(list.get(i), i);
            } else {
                instance = list;
            }
        } else if (reader.getToken() == JsonToken.ObjectStart) {
            addObjectMetadata (inst_type);
            ObjectMetadata t_data = object_metadata.get(inst_type);

            instance = inst_type.getClass().getConstructor().newInstance();

            while (true) {
                reader.Read ();

                if (reader.getToken() == JsonToken.ObjectEnd)
                    break;

                String property = (String) reader.getToken_value();

                if (t_data.fieldAndMethodsMetaData().containsKey (property)) {
                    //get value from method / field
                } else {
                    // find other ways to read value
                }
            }
        }

        return null; //still some more work to do
    }
}
