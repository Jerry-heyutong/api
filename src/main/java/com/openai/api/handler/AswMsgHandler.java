package com.openai.api.handler;

import java.io.InputStream;

/**
 * @Auther: HeYuTong
 * @Date: 2023/4/12 11:24
 * @Description:
 */

public interface AswMsgHandler {

    void actMsg(InputStream is, String line);

}