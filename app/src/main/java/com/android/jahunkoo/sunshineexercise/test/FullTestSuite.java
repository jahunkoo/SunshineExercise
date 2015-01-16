package com.android.jahunkoo.sunshineexercise.test;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by Jahun Koo on 2015-01-16.
 */
public class FullTestSuite extends TestSuite {

    public static Test suite() {
        return new TestSuiteBuilder(FullTestSuite.class).includeAllPackagesUnderHere().build();
    }
    public FullTestSuite() {
        super();
    }
}
