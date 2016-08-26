/****************************************************
* Name: Broken Link Checker
* Author: Josh Agda
* Date: 24/08/16
*
* Notes:
* - A great portion of the code is from 
* http://www.software-testing-tutorials-automation.com/2015/08/how-to-find-broken-linksimages-from.html
* and
* http://stackoverflow.com/questions/1201048/allowing-java-to-use-an-untrusted-certificate-for-ssl-https-connection
* - Make sure YOU HAVE REFERENCED the Selenium libraries in the project
* - Change the IE driver directory to fit your system
****************************************************/

package BrokenLinks;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;

public class BL {

	private static boolean pageOnly = true;

	private static Set<String> masterPageSet = new HashSet<String>();
	private static List<String> listOfLinks = new ArrayList<String>();
	private static List<String> pageQueue = new ArrayList<String>();
	private static List<String> validLinkList = new ArrayList<String>();
	private static List<String> brokenLinkList = new ArrayList<String>();

	private static int numOfValidLinks = 0;
	private static int numOfBrokenLinks = 0;

	public static void main(String args[]) {
		System.setProperty("webdriver.ie.driver",
				"C:\\Users\\agda.timothy@cgic.cooperators.ca\\Documents\\EclipseDev\\selenium-2.53.0\\IEDriverServer.exe");

		String customURL = "";
		String originalDomain = "";
		String pageOnlyAns = "";
		Scanner input = new Scanner(System.in);
		WebDriver mainDriver;

		System.out.println("Welcome to Josh's Broken Link Checker!");
		System.out.print("Enter URL Address: ");
		customURL = input.nextLine();
		System.out.print("One page only (Y/N): ");
		pageOnlyAns = input.nextLine();
		if (pageOnlyAns.equalsIgnoreCase("n") || pageOnlyAns.equalsIgnoreCase("no")) {
			pageOnly = false;
		}
		input.close();
		originalDomain = customURL.split("/")[0] + "//" + customURL.split("/")[2];

		mainDriver = new InternetExplorerDriver();
		mainDriver.manage().window().maximize();

		System.out.println("\nInitiating website scanning...");
		listOfLinks.add(customURL);
		masterPageSet.add(customURL);
		pageQueue.add(customURL);

		// Goes through each webpage and scans the links
		for (int i = 0; i < pageQueue.size(); i++) {
			System.out.println("current page: " + pageQueue.get(i));
			findLinksInPage(pageQueue.get(i), originalDomain, mainDriver);
		}

		mainDriver.close();

		// Removes duplicates (if any) before scan by converting to a HashSet
		// and back
		Set<String> noDuplLinks = new HashSet<String>(listOfLinks);
		listOfLinks.clear();
		listOfLinks.addAll(noDuplLinks);

		// Summarizes results
		if (listOfLinks.size() > 0) {
			scanPageCodes();
			
			Collections.sort(validLinkList);
			Collections.sort(brokenLinkList);

			System.out.println("--------------------------------------------");
			System.out.println("Scan Complete!");

			// Print out valid stats
			System.out.format("%d valid links found (%.2f%%):\n", numOfValidLinks,
					((double) numOfValidLinks / ((double) numOfValidLinks + (double) numOfBrokenLinks)) * 100);
			for (int i = 0; i < validLinkList.size(); i++)
				System.out.println(validLinkList.get(i));

			// Print out broken stats
			System.out.format("\n%d broken links found (%.2f%%):\n", numOfBrokenLinks,
					((double) numOfBrokenLinks / ((double) numOfValidLinks + (double) numOfBrokenLinks)) * 100);
			for (int i = 0; i < brokenLinkList.size(); i++)
				System.out.println(brokenLinkList.get(i));
		} else {
			System.out.println("--------------------------------------------");
			System.out.println("Scan Complete!");
			System.out.println("no links/URLs found");
		}
	}

	private static void findLinksInPage(String currentURL, String originalDomain, WebDriver driver) {
		String currentHREF = "";

		try {
			// Open page and scan links
			driver.get(currentURL);
			driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
			List<WebElement> totalLinks = driver.findElements(By.tagName("a"));

			for (int i = 0; i < totalLinks.size(); i++) {
				currentHREF = totalLinks.get(i).getAttribute("href");

				// Checks if the href is not nothing and is an actual link
				if (currentHREF != null && currentHREF.length() > 0 && !currentHREF.contains("javascript:")
						&& !currentHREF.contains("mailto:") && !currentHREF.contains("tel:")
						&& !currentHREF.contains("#")) {
					listOfLinks.add(currentHREF);

					// Add to list of pages to check for links if within the
					// same domain
					if (currentHREF.contains(originalDomain) && !currentHREF.contains(".pdf")
							&& !currentHREF.contains(".zip") && pageOnly == false) {
						if (!masterPageSet.contains(currentHREF)) {
							pageQueue.add(currentHREF);
						}

						masterPageSet.add(currentHREF);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("FATAL ERROR: " + e.getMessage());
		}
	}

	public static void scanPageCodes() {
		String url;

		System.out.println("\nTotal Number of links/URLs found = " + listOfLinks.size());
		System.out.println("--------------------------------------------\n");

		// for loop to open all links one by one to check response code.
		boolean isValid = false;
		for (int i = 0; i < listOfLinks.size(); i++) {
			url = listOfLinks.get(i);

			isValid = getResponseCode(url);

			if (isValid) {
				numOfValidLinks++;
				validLinkList.add(url);
				System.out.println("valid link: " + url + "\n");
			} else {
				numOfBrokenLinks++;
				brokenLinkList.add(url);
				System.out.println("BROKEN LINK - - - - - > " + url + "\n");
			}
		}
	}

	private static boolean getResponseCode(String currentURL) {

		boolean validResponse = false;
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

		if ((code == 404) || (code == 505)) {
			validResponse = false;
		} else {
			validResponse = true;
		}

		return validResponse;
	}
}
