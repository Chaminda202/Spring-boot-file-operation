package com.spring.fileopertion.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JacksonUtil {
	private static final Logger LOG = LoggerFactory.getLogger(JacksonUtil.class);
	private static ObjectMapper mapper = new ObjectMapper();

	public static String getToString(Object src ) {
		try {
			return mapper.writeValueAsString(src);
		} catch (JsonProcessingException e) {
			LOG.error("Error in convert object to json {}", e.getMessage());
		}
		return null;
	}
}
