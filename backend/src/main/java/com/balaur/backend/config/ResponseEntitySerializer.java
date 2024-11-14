package com.balaur.backend.config;

import com.balaur.backend.responses.SalariesResponseWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public class ResponseEntitySerializer extends GenericJackson2JsonRedisSerializer {
//    @Override
//    public Object deserialize(byte[] bytes) {
//        try {
//            JavaType type = this.getJavaType(ResponseEntity.class, SalariesResponseWrapper.class);
//            return this.getObjectMapper().readValue(bytes, type);
//        } catch (IOException e) {
//            throw new SerializationException("Error deserializing ResponseEntity", e);
//        }
//    }
//
//    @Override
//    public byte[] serialize(Object object) {
//        try {
//            return this.getObjectMapper().writeValueAsBytes(object);
//        } catch (JsonProcessingException e) {
//            throw new SerializationException("Error serializing ResponseEntity", e);
//        }
//    }
}
