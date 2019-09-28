package com.natebean.transformers;

import java.util.ArrayList;
import java.util.List;

import com.natebean.GapProductionLogSplitStream;
import com.natebean.models.GapLog;
import com.natebean.models.GapLogProductionLogSplitRecord;
import com.natebean.models.JSONSerde;
import com.natebean.models.ProductionLog;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;

public class GapProductionLogSplitTransformer
        implements ValueTransformer<GapLog, Iterable<GapLogProductionLogSplitRecord>> {

    // private ProcessorContext context;
    private ReadOnlyKeyValueStore<String, ValueAndTimestamp<String>> kvStore;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        // this.context = context;
        kvStore = (ReadOnlyKeyValueStore<String, ValueAndTimestamp<String>>) context
                .getStateStore(GapProductionLogSplitStream.STATE_STORE_NAME);

    }

    @Override
    public void close() {

    }

    @Override
    public Iterable<GapLogProductionLogSplitRecord> transform(GapLog gl) {

        String startRange = String.format("%d:%d", gl.sidId, gl.sysId);
        String endRange = String.format("%d:%d", gl.sidId, gl.sysId + 1);

        List<GapLogProductionLogSplitRecord> results = new ArrayList<>();
        JSONSerde<ProductionLog> js = new JSONSerde<>();

        KeyValueIterator<String, ValueAndTimestamp<String>> range = kvStore.range(startRange, endRange);
        System.out.print("*");
        while (range.hasNext()) {
            KeyValue<String, ValueAndTimestamp<String>> productionLogMessage = range.next();
            ValueAndTimestamp<String> plvt = productionLogMessage.value;
            ProductionLog pl = js.deserialize("nop", plvt.value().toString().getBytes());
            if (gl.startTime < pl.endTime && gl.endTime > pl.startTime && gl.sidId == pl.sidId
                    && gl.sysId == pl.sysId) {
                results.add(new GapLogProductionLogSplitRecord(gl, pl));
                System.out.print(".");
            }
        }

        // TODO handle a miss from range

        range.close();
        js.close();
        return results;
    }

}