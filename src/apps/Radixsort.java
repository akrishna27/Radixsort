package apps;

import java.io.IOException;
import java.util.Scanner;

import structures.Node;

/**
 * This class sorts a given list of strings which represent numbers in
 * the given radix system. For instance, radix=10 means decimal numbers;
 * radix=16 means hexadecimal numbers. 
 * 
 * @author ru-nb-cs112
 */
public class Radixsort {

	/**
	 * Master list that holds all items, starting with input, and updated after every pass
	 * of the radixsort algorithm. Holds sorted result after the final pass. This is a
	 * circular linked list in which every item is stored in its textual string form (even
	 * though the items represent numbers). This masterListRear field points to the last 
	 * node in the CLL.
	 */
	Node<String> masterListRear;
	
	/**
	 * Array of linked lists that holds the digit-wise distribution of the items during
	 * each pass of the radixsort algorithm. 
	 */
	Node<String>[] buckets;
	
	/** 
	 * The sort radix, defaults to 10.
	 */
	int radix=10;
	
	/**
	 * Initializes this object with the given radix (10 or 16)
	 * 
	 * @param radix
	 */
	public Radixsort() {
		masterListRear = null;
		buckets = null;
	}
	
	/**
	 * Sorts the items in the input file, and returns a CLL containing the sorted result
	 * in ascending order. The first line in the input file is the radix. Every subsequent
	 * line is a number, to be read in as a string.
	 * 
	 * The items in the input are first read and stored in the master list, which is a CLL that is referenced
	 * by the masterListRear field. Next, the max number of digits in the items is determined. Then, 
	 * scatter and gather are called, for each pass through the items. Pass 0 is for the least
	 * significant digit, pass 1 for the second-to-least significant digit, etc. After each pass,
	 * the master list is updated with items in the order determined at the end of that pass.
	 * 
	 * NO NEW NODES are created in the sort process - the nodes of the master list are recycled
	 * through all the intermediate stages of the sorting process.
	 * 
	 * @param sc Scanner that points to the input file of radix + items to be sorted
	 * @return Sorted (in ascending order) circular list of items
	 * @throws IOException If there is an exception in reading the input file
	 */
	public Node<String> sort(Scanner sc) 
	throws IOException {
		// first line is radix
		if (!sc.hasNext()) { // empty file, nothing to sort
			return null;
		}
		
		// read radix from file, and set up buckets for linked lists
		radix = sc.nextInt();
		buckets = (Node<String>[])new Node[radix];
		
		// create master list from input
		createMasterListFromInput(sc);
		
		// find the string with the maximum length
		int maxDigits = getMaxDigits();
		
		for (int i=0; i < maxDigits; i++) {
			scatter(i);
			gather();
		}
		
		return masterListRear;
	}
	
	/**
	 * Reads entries to be sorted from input file and stores them as 
	 * strings in the master CLL (pointed by the instance field masterListRear, 
	 * in the order in which they are read. In other words, the first entry in the linked 
	 * list is the first entry in the input, the second entry in the linked list is the 
	 * second entry in the input, and so on. 
	 * 
	 * @param sc Scanner pointing to the input file
	 * @throws IOException If there is any error in reading the input
	 */
	public void createMasterListFromInput(Scanner sc) 
	throws IOException {
		if (sc.nextLine() == null){
			throw new IOException();
		}
		String curr = sc.nextLine();
		masterListRear = new Node<String> (curr,null);
		masterListRear.next = masterListRear;
		while (sc.hasNextLine()){
			curr = sc.nextLine();
			Node<String> tmp = new Node<String> (curr,masterListRear.next);
			masterListRear.next = tmp;
			masterListRear = tmp;
		}
	}
	
	/**
	 * Determines the maximum number of digits over all the entries in the master list
	 * 
	 * @return Maximum number of digits over all the entries
	 */
	public int getMaxDigits() {
		int maxDigits = masterListRear.data.length();
		Node<String> ptr = masterListRear.next;
		while (ptr != masterListRear) {
			int length = ptr.data.length();
			if (length > maxDigits) {
				maxDigits = length;
			}
			ptr = ptr.next;
		}
		return maxDigits;
	}
	
	/**
	 * Scatters entries of master list (referenced by instance field masterListReat) 
	 * to buckets for a given pass.
	 * 
	 * Passes are digit by digit, starting with the rightmost digit -
	 * the rightmost digit is the "0-th", i.e. pass=0 for rightmost digit, pass=1 for 
	 * second to rightmost, and so on. 
	 * 
	 * Each digit is extracted as a character, 
	 * then converted into the appropriate numeric value in the given radix
	 * using the java.lang.Character.digit(char ch, int radix) method
	 * 
	 * @param pass Pass is 0 for rightmost digit, 1 for second to rightmost, etc
	 */
	public void scatter(int pass) {//CLEAR OUT THE BUCKETS
		Node<String> ptr = masterListRear.next;
		Node<String> first = masterListRear.next;
		Node<String> ptr2;
		for (int i = 0; i < buckets.length; i++){
			buckets[i] = null;
		}
		do{
			try{
				char c = ptr.data.charAt(ptr.data.length()-(pass+1));
				int i = Character.getNumericValue(c);
				if (buckets[i] == null){
					buckets[i] = ptr;
					ptr = ptr.next;
					buckets[i].next = buckets[i];
					continue;
				}
					ptr2 = ptr.next;
					ptr.next = buckets[i].next;
					buckets[i].next = ptr;
					buckets[i] = buckets[i].next;
					ptr = ptr2;
			}catch(IndexOutOfBoundsException e){
				if (buckets[0] == null){
					ptr2 = ptr.next;
					buckets[0] = ptr;
					buckets[0].next = buckets[0];
					ptr = ptr2;
					continue;
				}
				ptr2 = ptr.next;
				ptr.next = buckets[0].next;
				buckets[0].next = ptr;
				ptr = ptr2;
				buckets[0] = buckets[0].next;
			}
			/*
			 *PROBLEM
			 *take a look at the scatter and gather because output is wrong
			 */
		}while (!ptr.equals(first));
	}

	/**
	 * Gathers all the CLLs in all the buckets into the master list, referenced
	 * by the instance field masterListRear
	 * 
	 * @param buckets Buckets of CLLs
	 */
	public void gather() {
		Node<String> tmp;
		masterListRear = null;
		for (int i = 0; i < buckets.length; i++){
			if (buckets[i]!= null){
				if (masterListRear == null){
					masterListRear = buckets[i];
					masterListRear.next = buckets[i].next;
					continue;
				}
				tmp = buckets[i].next;
				buckets[i].next = masterListRear.next;
				masterListRear.next = tmp;
				masterListRear = buckets[i];
			}
		}
	}	
}

