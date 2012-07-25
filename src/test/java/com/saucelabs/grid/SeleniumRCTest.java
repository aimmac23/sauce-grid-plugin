package com.saucelabs.grid;

import com.thoughtworks.selenium.DefaultSelenium;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Ross Rowe
 */
public class SeleniumRCTest {

    private DefaultSelenium selenium;

    @BeforeMethod
    public void setUp() throws Exception {

        DefaultSelenium selenium = new DefaultSelenium(
                "localhost",
                4444,
                "{\"username\":\"rossco_9_9\"," +
                        "\"access-key\":\"44f0744c-1689-4418-af63-560303cbb37b\"," +
                        "\"os\":\"Windows 2003\"," +
                        "\"browser\":\"firefox\"," +
                        "\"browser-version\":\"7\"," +
                        "\"name\":\"Testing Selenium 1 with Java on Sauce\"}",
                "http://www.amazon.com/");
        selenium.start();
        this.selenium = selenium;

    }

    @Test
    public void selenumRC() throws Exception {
        this.selenium.open("http://www.amazon.com");
        assertEquals("Amazon.com: Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more", this.selenium.getTitle());
    }

    @AfterMethod
    public void tearDown() throws Exception {
        this.selenium.stop();
    }

}
