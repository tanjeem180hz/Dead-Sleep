package com.deadsleep.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.deadsleep.app.model.UpdateInfo;

import org.junit.Test;

public class UpdateManagerTest {

    @Test
    public void testParseUpdateInfoJson() {
        String json = "{\n" +
                "  \"versionCode\": 2,\n" +
                "  \"versionName\": \"1.0.1\",\n" +
                "  \"apkUrl\": \"https://github.com/tanjeem180hz/Dead-Sleep/raw/main/release/DeadSleep-v1.0.0.apk\",\n" +
                "  \"forceUpdate\": true,\n" +
                "  \"title\": \"Mandatory Update Available 🚀\",\n" +
                "  \"releaseNotes\": \"• Critical performance enhancements\\n• Clock accuracy fixes\",\n" +
                "  \"minSupportedVersionCode\": 2\n" +
                "}";

        UpdateInfo info = UpdateInfo.fromJson(json);
        assertNotNull(info);
        assertEquals(2, info.getVersionCode());
        assertEquals("1.0.1", info.getVersionName());
        assertEquals("https://github.com/tanjeem180hz/Dead-Sleep/raw/main/release/DeadSleep-v1.0.0.apk", info.getApkUrl());
        assertTrue(info.isForceUpdate());
        assertEquals("Mandatory Update Available 🚀", info.getTitle());
        assertTrue(info.getReleaseNotes().contains("Clock accuracy fixes"));
        assertEquals(2, info.getMinSupportedVersionCode());
    }

    @Test
    public void testVersionComparisonLogic() {
        int installedVersionCode = 1;

        UpdateInfo newer = new UpdateInfo();
        newer.setVersionCode(2);
        newer.setForceUpdate(true);

        UpdateInfo same = new UpdateInfo();
        same.setVersionCode(1);
        same.setForceUpdate(true);

        assertTrue("Newer version must trigger update", newer.getVersionCode() > installedVersionCode);
        assertFalse("Same version must not trigger update", same.getVersionCode() > installedVersionCode);
    }

    @Test
    public void testMalformedJsonHandling() {
        UpdateInfo invalid = UpdateInfo.fromJson("{ broken json }");
        // Must return null without throwing unhandled exceptions
        assertEquals(null, invalid);
    }
}
