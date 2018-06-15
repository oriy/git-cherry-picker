package com.jacky.git

import com.google.gson.internal.StringMap;
import groovy.transform.EqualsAndHashCode

import java.time.ZonedDateTime

/**
 * User: oriy
 * Date: 14/05/2017
 */
@EqualsAndHashCode
public class PullRequestStatus {
    public static final String ID = 'id'
    public static final String URL = 'url'
    public static final String STATE = 'state'
    public static final String DESCRIPTION = 'description'
    public static final String TARGET_URL = 'target_url'
    public static final String CONTEXT = 'context'
    public static final String CREATED_AT = 'created_at'
    public static final String UPDATED_AT = 'updated_at'

    long id
    String url
    String state
    String description
    String targetUrl
    String context
    ZonedDateTime createdAt
    ZonedDateTime updatedAt

    public PullRequestStatus() {}

    public PullRequestStatus(StringMap<Object> objectStringMap) {
        id = ((Number)objectStringMap.get(ID)).longValue();
        url = objectStringMap.get(URL)
        state = objectStringMap.get(STATE)
        description = objectStringMap.get(DESCRIPTION)
        targetUrl = objectStringMap.get(TARGET_URL)
        context = objectStringMap.get(CONTEXT)
        createdAt = ZonedDateTime.parse(objectStringMap.get(CREATED_AT).toString())
        updatedAt = ZonedDateTime.parse(objectStringMap.get(UPDATED_AT).toString())
    }
}
