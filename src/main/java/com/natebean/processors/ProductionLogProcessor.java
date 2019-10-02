package com.natebean.processors;

import com.natebean.models.ProductionLog;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

public class ProductionLogProcessor implements Processor<String, ProductionLog> {

    private String prefix;

    @Override
    public void init(ProcessorContext context) {
        // TODO Auto-generated method stub

    }

    public ProductionLogProcessor(String prefix){
        this.prefix = prefix;
    }

    @Override
    public void process(String key, ProductionLog value) {
        System.out.println(prefix + ": " + key);

    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

}

