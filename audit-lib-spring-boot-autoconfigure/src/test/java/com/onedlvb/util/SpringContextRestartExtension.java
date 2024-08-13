package com.onedlvb.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.test.context.TestContextManager;

/**
 * Extension for restarting context
 * @author Matushkin Anton
 */
public class SpringContextRestartExtension implements TestInstancePostProcessor, TestWatcher {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        TestContextManager testContextManager = new TestContextManager(testInstance.getClass());
        SpringRestarter.getInstance().init(testContextManager);
    }

}
