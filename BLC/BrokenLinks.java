/****************************************************
* Name: Broken Link Checker (General)
* Author: Timothy Agda
* Date: 24/08/16
*
* Notes:
* - A great portion of the code is from 
* http://www.software-testing-tutorials-automation.com/2015/08/how-to-find-broken-linksimages-from.html
* and
* http://stackoverflow.com/questions/1201048/allowing-java-to-use-an-untrusted-certificate-for-ssl-https-connection
* - Make sure YOU HAVE REFERENCED the Selenium libraries in the project
* - Change the Firefox browser directory to fit your system
****************************************************/

package genBrokenLinks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
//import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import genBrokenLinks.webcrawlInfo;

public class BrokenLinks {

	// Browser + driver locations
	private static final String IEdriver = "IEDriverServer.exe";
	private static final String chromedriver = "chromedriver.exe";
//	private static final String FFbrowser = "C:\\Users\\agda.timothy@cgic.cooperators.ca\\AppData\\Local\\Mozilla Firefox\\firefox.exe";
	
	private static String foldername = "";
	private static int screenshotCounter = 2;

	public static void main(String args[]) throws IOException {
		System.setProperty("webdriver.ie.driver", IEdriver);
		System.setProperty("webdriver.chrome.driver",chromedriver);
//		System.setProperty("webdriver.firefox.bin",FFbrowser);

		boolean pageOnly = true;

		String customURL = "";
		String originalDomain = "";
		String pageOnlyAns = "";
		String browserChoice = "";
		Scanner input = new Scanner(System.in);

		WebDriver mainDriver;
		webcrawlInfo currentLinksInfo = new webcrawlInfo();

		File newFolder;
		FileWriter outputFile;
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		Calendar cal = Calendar.getInstance();
		
		
		// Get inputs (URL + browser + onepageChoice)
		System.out.println("Welcome to The Co-operators Broken Link Checker!");
		System.out.print("Enter URL Address: ");
		customURL = input.nextLine();
		System.out.println("1) IE (default)");
		System.out.println("2) Firefox");
		System.out.println("3) Chrome");
		System.out.print("Select which browser to run on: ");
		browserChoice = input.nextLine();
		System.out.print("One page only (Y/N): ");
		pageOnlyAns = input.nextLine();
		System.out.println();
		if (pageOnlyAns.equalsIgnoreCase("n") || pageOnlyAns.equalsIgnoreCase("no")) {
			pageOnly = false;
		}
		input.close();
		
		// Create original domain from URL
		originalDomain = customURL.split("/")[0] + "//" + customURL.split("/")[2];

		// Create folder + results file
		foldername = customURL.split("/")[2] + "_" + dateFormat.format(cal.getTime()) + ".xls";
		newFolder = new File(foldername);
		newFolder.mkdir();
		outputFile = new FileWriter(foldername + "\\results.xls");
		outputFile.write("URL\tResponse Code\tValid?\n");

		// Chooses browser
		switch (browserChoice.toLowerCase()) {
		case "2":
		case "firefox":
			mainDriver = new FirefoxDriver();
			break;
		case "3":
		case "chrome":
			mainDriver = new ChromeDriver();
			break;
		default:
			mainDriver = new InternetExplorerDriver();				
		}
		mainDriver.manage().window().maximize();

		// Initializing scan
		System.out.println("Initiating website scanning...");
		currentLinksInfo.addToLinkList(customURL);
		currentLinksInfo.addNewPage(customURL);

		// Goes through each webpage and scans the links
		for (int i = 0; i < currentLinksInfo.getPageQueueSize(); i++) {
			System.out.println("current page [" + (i + 1) + "] (" + currentLinksInfo.getLinkListSize() + " link(s) found): " + currentLinksInfo.getPageQueueElement(i));
			try {
				findLinksInPage(currentLinksInfo.getPageQueueElement(i), originalDomain, mainDriver, currentLinksInfo, pageOnly);
			} catch (Exception e) {
				break;
			}
		}

		mainDriver.quit();
		
		currentLinksInfo.removeDuplLinkList();
		currentLinksInfo.sortLinkList();
		printResults(browserChoice, mainDriver, currentLinksInfo, outputFile);
		
		outputFile.close();
	}

	private static void findLinksInPage(String currentURL, String originalDomain, WebDriver driver,
			webcrawlInfo currentLinksInfo, boolean pageOnly) {
		String currentHREF = "";

		// Open page and scan links
		driver.get(currentURL);
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		List<WebElement> totalLinks = driver.findElements(By.tagName("a"));

		for (int i = 0; i < totalLinks.size(); i++) {
			currentHREF = totalLinks.get(i).getAttribute("href");

			// Error checking the link
			if (currentHREF != null && currentHREF.length() > 0 && !currentHREF.contains("javascript:")
					&& !currentHREF.contains("mailto:") && !currentHREF.contains("tel:")
					&& !currentHREF.contains("#")) {
				currentLinksInfo.addToLinkList(currentHREF);

				// Add to list of pages to check for links if within the
				// same domain
				if (currentHREF.contains(originalDomain) && !currentHREF.contains(".pdf")
						&& !currentHREF.contains(".zip") && !currentHREF.contains(".docx") && pageOnly == false) {
					if (!currentLinksInfo.masterPageSetContains(currentHREF)) {
						currentLinksInfo.addToPageQueue(currentHREF);
					}

					currentLinksInfo.addToMasterPageSet(currentHREF);
				}
			}
		}
	}

	private static void printResults(String browserChoice, WebDriver driver, webcrawlInfo currentLinksInfo, FileWriter outputFile) throws IOException {
		
		// Summarizes results
		if (currentLinksInfo.getLinkListSize() > 0) {
			scanPageCodes(browserChoice, currentLinksInfo, outputFile);

			currentLinksInfo.sortValidList();
			currentLinksInfo.sortBrokenList();

			System.out.println("--------------------------------------------");
			System.out.println("Scan Complete!");

			System.out.println((currentLinksInfo.getNumOfValidLinks() + currentLinksInfo.getNumOfBrokenLinks())
					+ " total links scanned\n");

			// Print out valid scan
			System.out.format("%d valid links found (%.2f%%):\n", currentLinksInfo.getNumOfValidLinks(),
					((double) currentLinksInfo.getNumOfValidLinks() / ((double) currentLinksInfo.getNumOfValidLinks()
							+ (double) currentLinksInfo.getNumOfBrokenLinks())) * 100);
			for (int i = 0; i < currentLinksInfo.getValidLinkListSize(); i++)
				System.out.println(currentLinksInfo.getValidLinkElement(i));

			// Print out broken scan
			System.out.format("\n%d broken links found (%.2f%%):\n", currentLinksInfo.getNumOfBrokenLinks(),
					((double) currentLinksInfo.getNumOfBrokenLinks() / ((double) currentLinksInfo.getNumOfValidLinks()
							+ (double) currentLinksInfo.getNumOfBrokenLinks())) * 100);
			for (int i = 0; i < currentLinksInfo.getBrokenLinkListSize(); i++)
				System.out.println((currentLinksInfo.getBrokenLinkElement(i)));
		} else {
			System.out.println("--------------------------------------------");
			System.out.println("Scan Complete!");
			System.out.println("no links/URLs found");
		}
	}

	public static void scanPageCodes(String browserChoice, webcrawlInfo currentLinksInfo, FileWriter outputFile) throws IOException {
		
		String url;
		int code = 404;
		
		// Uncomment all the code in this function to allow screenshots (runs slower)
//		WebDriver driver;
		
		currentLinksInfo.sortLinkList();

//		System.out.println("\n");
//		switch (browserChoice.toLowerCase()) {
//		case "2":
//		case "firefox":
//			driver = new FirefoxDriver();
//			break;
//		case "3":
//		case "chrome":
//			driver = new ChromeDriver();
//			break;
//		default:
//			driver = new InternetExplorerDriver();				
//		}
//		driver.manage().window().maximize();
		System.out.println("");

		System.out.println("\nTotal Number of links/URLs found = " + currentLinksInfo.getLinkListSize());
		System.out.println("--------------------------------------------\n");

		// for loop to open all links one by one to check response code.
		for (int i = 0; i < currentLinksInfo.getLinkListSize(); i++) {
			url = currentLinksInfo.getLinkListElement(i);

			System.out.format("(%.2f%%)", ((double) (i) / (double) (currentLinksInfo.getLinkListSize()) * 100));
			code = getResponseCode(url);

			if (!((code == 404) || (code == 505))) {
				currentLinksInfo.incrementNumOfValidLinks();
				currentLinksInfo.addToValidList(url);
				System.out.println("valid link: " + url + "\n");
				outputFile.write(url + "\t" + code + "\tYes\n");
				outputFile.flush();
			} else {
				currentLinksInfo.incrementNumOfBrokenLinks();
				currentLinksInfo.addToBrokenList(url);
				System.out.println("BROKEN LINK - - - - - > " + url + "\n");
				outputFile.write(url + "\t" + code + "\tNO\n");
				outputFile.flush();
			}

//			driver.get(url);
//			driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
//			takeScreenshot(driver);
		}

//		driver.quit();
	}

	private static int getResponseCode(String currentURL) {
		int code = 404;

		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
			} };

			// Try installing the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Try Https connection without having the certificate in
			// the truststore
			URL url = new URL(currentURL);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			code = connection.getResponseCode();
			System.out.println("Response Code Is : " + code);

		} catch (Exception e) {

			// Try a regular Http connection
			try {
				URL url = new URL(currentURL);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.connect();
				code = connection.getResponseCode();
				System.out.println("Response Code Is : " + code);
			} catch (Exception e2) {
				System.out.println("ERROR: " + e2.getMessage());
			}
		}

		return code;
	}

	private static void takeScreenshot(WebDriver driver) {
		File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

		try {
			FileUtils.copyFile(scrFile, new File(foldername + "\\" + screenshotCounter++ + ".png"));
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
