package com.pw.awairbridge.client.dto.awair;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.HashMap;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Setter
@ToString(callSuper = false)
public class AwairDataPacket {
    // Capture all other fields that Jackson do not match other members. Here we are doing straight
    // passthru i.e. we don't really care what fields awair is returning because we don't
    // do any processing at all, so we just capture everything and pass it along to PW
    Map<String, Object> otherFields = new HashMap<>();

    @JsonAnySetter
    public void setOtherFields(String name, Object value) {
        otherFields.put(name, value);
    }

    @JsonUnwrapped
    @JsonAnyGetter
    public Map<String, Object> getOtherFields() {
        return otherFields;
    }
}
