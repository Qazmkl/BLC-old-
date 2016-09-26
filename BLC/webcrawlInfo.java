/****************************************************
* Name: Web Crawl Info Object
* Author: Timothy Agda
* Date: 23/08/16
****************************************************/

package genBrokenLinks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class webcrawlInfo {

	private int numOfValidLinks;
	private int numOfBrokenLinks;
	
	private Set<String> linkSet;
	private Set<String> masterPageSet;
	private List<String> pageQueue;
	private List<String> listOfLinks;
	private List<String> validLinkList;
	private List<String> brokenLinkList;
	
	public webcrawlInfo() {
		numOfValidLinks = 0;
		numOfBrokenLinks = 0;
		
		linkSet = new HashSet<String>();
		masterPageSet = new HashSet<String>();
		pageQueue = new ArrayList<String>();
		listOfLinks = new ArrayList<String>();
		validLinkList = new ArrayList<String>();
		brokenLinkList = new ArrayList<String>();
	}
	
	// NUM OF VALID LINKS
	public void incrementNumOfValidLinks() {
		numOfValidLinks++;
	}
	public int getNumOfValidLinks() {
		return numOfValidLinks;
	}
	
	// NUM OF BROKEN LINKS
	public void incrementNumOfBrokenLinks() {
		numOfBrokenLinks++;
	}
	public int getNumOfBrokenLinks() {
		return numOfBrokenLinks;
	}
	
	// LINK SET
	public void addToLinkSet(String toInsert) {
		linkSet.add(toInsert);
	}	
	public void clearLinkSet() {
		linkSet.clear();
	}
	public boolean linkSetContains(String element) {
		return linkSet.contains(element);
	}
	public int getLinkSetSize() {
		return linkSet.size();
	}
	
	// MASTER PAGE SET
	public void addToMasterPageSet(String toInsert) {
		masterPageSet.add(toInsert);
	}
	public void clearMasterPageSet() {
		masterPageSet.clear();
	}
	public boolean masterPageSetContains(String element) {
		return masterPageSet.contains(element);
	}
	public int getMasterPageSetSize() {
		return masterPageSet.size();
	}
	
	// PAGE QUEUE
	public void addToPageQueue(String toInsert) {
		pageQueue.add(toInsert);
	}
	public void clearPageQueue() {
		pageQueue.clear();
	}
	public String getPageQueueElement(int element) {
		return pageQueue.get(element);
	}
	public int getPageQueueSize() {
		return pageQueue.size();
	}
	
	// MASTER PAGE SET AND PAGE QUEUE
	public void addNewPage(String newPage) {
		addToMasterPageSet(newPage);
		addToPageQueue(newPage);
	}
	public void resetPageSetAndQueue(String startingPage) {
		clearMasterPageSet();
		clearPageQueue();
		addToMasterPageSet(startingPage);
		addToPageQueue(startingPage);
	}
	
	// LIST OF LINKS
	public void addToLinkList(String toInsert) {
		listOfLinks.add(toInsert);
	}
	public void clearLinkList() {
		listOfLinks.clear();
	}
	public void removeDuplLinkList() {
		Set<String> noDuplLinks = new HashSet<String>(listOfLinks);
		listOfLinks.clear();
		listOfLinks.addAll(noDuplLinks);
	}
	public void sortLinkList() {
		Collections.sort(listOfLinks);
	}
	public String getLinkListElement(int element) {
		return listOfLinks.get(element);
	}
	public int getLinkListSize() {
		return listOfLinks.size();
	}
	
	// VALID LINK LIST
	public void addToValidList(String toInsert) {
		validLinkList.add(toInsert);
	}
	public void clearValidList() {
		validLinkList.clear();
	}
	public void sortValidList() {
		Collections.sort(validLinkList);
	}
	public String getValidLinkElement(int element) {
		return validLinkList.get(element);
	}
	public int getValidLinkListSize() {
		return validLinkList.size();
	}
	
	// BROKEN LINK LIST
	public void addToBrokenList(String toInsert) {
		brokenLinkList.add(toInsert);
	}
	public void sortBrokenList() {
		Collections.sort(brokenLinkList);
	}
	public void clearBrokenList() {
		brokenLinkList.clear();
	}
	public String getBrokenLinkElement(int element) {
		return brokenLinkList.get(element);
	}
	public int getBrokenLinkListSize() {
		return brokenLinkList.size();
	}
}
