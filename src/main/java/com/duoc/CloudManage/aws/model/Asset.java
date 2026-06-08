package com.duoc.CloudManage.aws.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Asset {

    String name;
    String key;
    String url;
}
