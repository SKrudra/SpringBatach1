package com.example.batch.partitions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class RangePartitioner implements Partitioner {

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {

		Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
		for (int i = 1; i <= gridSize; i++) {
			ExecutionContext exContext = new ExecutionContext();
			exContext.put("filename", "input" + i + ".csv");
			exContext.put("name", "Thread" + i);
			result.put("partition" + i, exContext);
		}
		return result;
	}

}
