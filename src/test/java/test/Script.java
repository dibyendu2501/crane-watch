package test;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import data.CommonFunctionality;
import data.Constants;

public class Script {
  CommonFunctionality cf = new CommonFunctionality();

  @BeforeSuite
  public void setup() {
    cf.manageDataFiles();
    cf.initializeDriver();
  }

  @DataProvider(name = "cities")
  public Object[][] getCraneWatchCities() {
    return Constants.craneCities;
  }

  @Test(dataProvider = "cities")
  public void checkForCurrentPins(String city, String url) {
    cf.switchToMapFrame(url);
    cf.getDetailsOfAllPins(city);
    if (!cf.newPins.isEmpty()) {
      cf.writeDataInXlsx(city);
    }
  }

  @AfterSuite
  public void endTest() {
    cf.writePinDetailsJson();
    cf.driver.quit();
  }

}