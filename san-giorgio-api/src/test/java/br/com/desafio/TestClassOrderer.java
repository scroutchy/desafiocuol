package br.com.desafio;

import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Comparator;

public class TestClassOrderer implements ClassOrderer {
    @Override
    public void orderClasses(ClassOrdererContext classOrdererContext) {
        classOrdererContext.getClassDescriptors().sort(Comparator.comparingInt(this::weight));
    }

    private int weight(ClassDescriptor classDescriptor) {
        if (classDescriptor.isAnnotated(SpringBootTest.class)) {
            return 2;
        } else {
            return 1;
        }
    }
}
