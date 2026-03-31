package com.enterprise.ai.common.constants;

public class Constants {

    private Constants() {
    }

    public static final Integer SUCCESS_CODE = 200;

    public static final String SUCCESS_MSG = "操作成功";

    public static final Integer ERROR_CODE = 500;

    public static final String ERROR_MSG = "操作失败";

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String TOKEN_HEADER = "Authorization";

    public static final String USER_ID_KEY = "userId";

    public static final String USER_NAME_KEY = "userName";

    public static final Long TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60L;

    public static final Long CACHE_EXPIRE_TIME = 30 * 60L;

    public static final Integer PAGE_SIZE_DEFAULT = 10;

    public static final Integer PAGE_SIZE_MAX = 100;

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    public static final String TRACE_ID_KEY = "traceId";
}
