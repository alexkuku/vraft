package com.vraft.test.config;

import com.vraft.core.config.ConfigHolder;
import com.vraft.facade.config.ConfigServer;
import com.vraft.facade.system.SystemCtx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jweihsz
 * @version 2024/2/22 20:23
 **/
public class CfgParserTest {
    private final static Logger logger = LogManager.getLogger(CfgParserTest.class);

    private SystemCtx sysCtx;
    private ConfigServer configServer;

    @Before
    public void bf() {
        sysCtx = new SystemCtx();
        configServer = new ConfigHolder(sysCtx);
    }

    @Test
    public void testLoadCfgPath() {
        logger.info("config path:{}", configServer.getCfgFile());
    }

}
