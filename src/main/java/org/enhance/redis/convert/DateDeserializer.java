package org.enhance.redis.convert;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.enhance.redis.helper.RedisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * 带时区的反序列化
 *
 * @author Mr_wenpan@163.com 2021/8/7 3:01 下午
 */
public class DateDeserializer extends JsonDeserializer<Date> {

    private static final Logger logger = LoggerFactory.getLogger(DateDeserializer.class);

    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance(RedisHelper.DATE_FORMAT);

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        try {
            return DATE_FORMAT.parse(jsonParser.getValueAsString());
        } catch (ParseException e) {
            logger.warn("date format error : {}", ExceptionUtils.getStackTrace(e));
            return null;
        }
    }
}
