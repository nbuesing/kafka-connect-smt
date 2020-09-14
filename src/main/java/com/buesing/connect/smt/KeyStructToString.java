package com.buesing.connect.smt;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.Transformation;

import java.util.Map;
import java.util.stream.Collectors;

public class KeyStructToString<R extends ConnectRecord<R>> implements Transformation<R> {

    private static final ConfigDef CONFIG_DEF = new ConfigDef();

    @Override
    public void configure(Map<String, ?> props) {
    }

    @Override
    public R apply(R record) {

        final Schema schema = record.keySchema();

        if (schema == null) {
            throw new RuntimeException("this SMT only works on keys with schemas.");
        }

        final Struct struct = (Struct) record.key();

        return record.newRecord(record.topic(), record.kafkaPartition(), Schema.STRING_SCHEMA, asString(schema, struct), record.valueSchema(), record.value(), record.timestamp());
    }

    @Override
    public void close() {
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    private static String asString(final Schema schema, final Struct struct) {
        return schema.fields().stream()
                .map(field -> {
                    if (field.schema().type().isPrimitive()) {
                        Object value = struct.get(field);
                        return (value != null) ? value.toString() : "";
                    } else {
                        throw new RuntimeException("does not work on non-primitive fields.");
                    }
                })
                .collect(Collectors.joining("_"));
    }

}

