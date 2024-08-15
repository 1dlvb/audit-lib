package com.onedlvb.util;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContextManager;

/**
 * Util for Restarting context
 * @author Matushkin Anton
 */
public class SpringRestarter {

    private static SpringRestarter INSTANCE = null;

    private TestContextManager testContextManager;

    public static SpringRestarter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SpringRestarter();
        }

        return INSTANCE;
    }

    public void init(TestContextManager testContextManager) {
        this.testContextManager = testContextManager;
    }

    public void restart(Runnable stoppedLogic) {
        testContextManager.getTestContext().markApplicationContextDirty(DirtiesContext.HierarchyMode.EXHAUSTIVE);

        if (stoppedLogic != null) {
            stoppedLogic.run();
        }

        testContextManager.getTestContext().getApplicationContext();
    }

}