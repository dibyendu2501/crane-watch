package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.DriverManagerType;
import io.github.bonigarcia.wdm.WebDriverManager;

public class CommonFunctionality {
  private static final Logger LOG = LoggerFactory.getLogger(CommonFunctionality.class);

  List<String> newRows = new ArrayList<String>();

  public WebDriver driver;
  public JSONObject allPins = new JSONObject();
  public JSONObject newPins = new JSONObject();
  public JSONParser parser = new JSONParser();

  public void writeDataInXlsx(String city) {

    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet(city);

    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 12);
    headerFont.setColor(IndexedColors.BLUE.getIndex());

    CellStyle headerCellStyle = workbook.createCellStyle();
    CellStyle cellStyle = workbook.createCellStyle();

    headerCellStyle.setFont(headerFont);

    String[] dataArray;
    Row headerRow = null;
    Row dataRow = null;
    Cell cell = null;

    for (int j = 0; j <= newRows.size(); j++) {
      if (j == 0) {
        headerRow = sheet.createRow(j);
        for (int i = 0; i < Constants.columns.length; i++) {
          cell = headerRow.createCell(i);
          cell.setCellValue(Constants.columns[i]);
          cell.setCellStyle(headerCellStyle);
        }
      } else {
        dataRow = sheet.createRow(j);
        dataArray = newRows.get(j - 1).split("<separator>");
        for (int k = 0; k < dataArray.length; k++) {
          cell = dataRow.createCell(k);
          cellStyle.setWrapText(true);
          cell.setCellValue(dataArray[k]);
          cell.setCellStyle(cellStyle);
        }
      }
    }
    sheet.setDefaultColumnWidth(30);

    try {
      File baseXlsxFile = new File(System.getProperty("user.dir") + File.separator + "data-files"
          + File.separator + "Crane-Watch-Latest.xlsx");
      baseXlsxFile.delete();
      FileOutputStream outFile = new FileOutputStream(new File(System.getProperty("user.dir")
          + File.separator + "data-files" + File.separator + "Crane-Watch-Latest.xlsx"));
      workbook.write(outFile);
      outFile.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void manageDataFiles() {
    File newJsonFile = new File(System.getProperty("user.dir") + File.separator + "data-files"
        + File.separator + "crane-watch-latest.json");
    File oldJsonFile = new File(System.getProperty("user.dir") + File.separator + "data-files"
        + File.separator + "crane-watch-previous.json");

    File latestXlsxFile = new File(System.getProperty("user.dir") + File.separator + "data-files"
        + File.separator + "Crane-Watch-Latest.xlsx");

    oldJsonFile.delete();

    latestXlsxFile.delete();

    if (newJsonFile.renameTo(oldJsonFile)) {
      System.out.println("File renamed successfully");
    } else {
      System.out.println("Failed to rename file");
    }
  }

  public void getDetailsOfAllPins(String city) {
    String title;
    WebElement table;
    List<String> titles = new ArrayList<String>();
    List<String> values = new ArrayList<String>();
    String dataString;
    JSONObject cityPins = new JSONObject();
    JSONObject newCityPins = new JSONObject();
    List<WebElement> pins = driver.findElements(By.cssSelector("image[fill-opacity='0']"));
    WebDriverWait wait = new WebDriverWait(driver, 5000);
    // for (int i = 0; i < 5; i++) {
    for (WebElement pin : pins) {
      Actions actions = new Actions(driver);
      actions.moveToElement(pin).click().perform();
      // actions.moveToElement(pins.get(i)).click().perform();
      wait.until(ExpectedConditions
          .visibilityOfElementLocated(By.cssSelector("div[dojoattachpoint='_title']")));
      title = driver.findElement(By.cssSelector("div[dojoattachpoint='_title']")).getText();

      // LOG.info("\nFound title - {}", title);
      System.out.println("\nFound title - " + title);

      table = driver.findElement(By.cssSelector("table.attrTable"))
          .findElement(By.tagName("tbody"));
      for (WebElement row : table.findElements(By.tagName("tr"))) {
        titles.add(row.findElement(By.cssSelector("td.attrName")).getText());
        values.add(row.findElement(By.cssSelector("td.attrValue")).getText());
      }
      dataString = arrangeDataInOrder(titles, values);
      titles.clear();
      values.clear();
      cityPins.put(title, dataString);
      if (checkIfNewValue(dataString)) {
        newRows.add(dataString);
        newCityPins.put(title, dataString);
      }
      driver.findElement(By.cssSelector("div[class='titleButton close'][title='Close']")).click();
      driver.findElement(By.cssSelector("div[data-dojo-attach-point='_homeNode']")).click();
    }
    if (!cityPins.isEmpty())
      allPins.put(city, cityPins);
    if (!newCityPins.isEmpty())
      newPins.put(city, newCityPins);
  }

  public void writePinDetailsJson() {
    try {
      FileWriter writer = new FileWriter(System.getProperty("user.dir") + File.separator
          + "data-files" + File.separator + "crane-watch-latest.json");
      writer.write(allPins.toString());
      writer.close();

      writer = new FileWriter(System.getProperty("user.dir") + File.separator + "data-files"
          + File.separator + "New-pins.json");
      writer.write(newPins.toString());
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String arrangeDataInOrder(List<String> titles, List<String> values) {
    StringBuffer sb = new StringBuffer("");
    String data = null;
    for (int i = 0; i < titles.size(); i++) {
      if (Arrays.asList(Constants.columns).contains(titles.get(i))) {
        data = values.get(titles.indexOf(Constants.columns[i])).replace("N/A", "NA");
        if (data == null)
          data = "NA";
        if (i == 0) {
          sb.append(data);
        } else {
          sb.append("<separator>" + data);
        }
      }
    }
    return sb.toString();
  }

  public void initializeDriver() {
    WebDriverManager.getInstance(DriverManagerType.CHROME).setup();
    ChromeOptions co = new ChromeOptions();
    co.addArguments("--start-maximized");
    co.addArguments("--disable-infobars");
    co.addArguments("--disable-notifications");
    co.addArguments("--deny-permission-prompts");    
    driver = new ChromeDriver(co);
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    driver.manage().window().maximize();
  }

  public void switchToMapFrame(String url) {
    driver.get(url);
    driver.findElement(By.xpath("//a[@data-ct='OPT: View Map Asset']")).click();
    List<WebElement> frames = driver.findElements(By.tagName("iframe"));
    if (!frames.isEmpty()) {
      for (int f = 0; f < frames.size(); f++) {
        driver.switchTo().frame(f); // passing frame element didn't work here
        List<WebElement> pins = driver.findElements(By.cssSelector("image[fill-opacity='0']"));
        // LOG.info("Found total pins - {}", pins.size());
        System.out.println("Found total pins - " + pins.size());
        if (pins.size() != 0)
          break;
        else
          driver.switchTo().defaultContent();
      }
    }
  }

  public boolean checkIfNewValue(String value) {
    boolean isNew = true;
    try {
      JSONObject jsonObject = (JSONObject) parser
          .parse(new FileReader(System.getProperty("user.dir") + File.separator + "data-files"
              + File.separator + "crane-watch-previous.json"));
      if (jsonObject.toString().contains(value))
        isNew = false;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return isNew;
  }
}
